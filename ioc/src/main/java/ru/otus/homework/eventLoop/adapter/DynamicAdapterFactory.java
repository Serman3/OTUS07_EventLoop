package ru.otus.homework.eventLoop.adapter;

import com.sun.codemodel.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import ru.otus.homework.eventLoop.generator.MetaData;
import ru.otus.homework.eventLoop.generator.MetaDataCollector;
import ru.otus.homework.eventLoop.generator.MethodBuilder;
import ru.otus.homework.eventLoop.util.ApplicationProperties;

import javax.tools.*;
import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

@Slf4j
public class DynamicAdapterFactory<T> {

    private final static Properties PROPERTIES = ApplicationProperties.loadProperties();
    private final static String GENERATED_SOURCES_DIR = PROPERTIES.getProperty("generated.sources.dir");
    private final static String COMPILED_CLASSES_DIR = PROPERTIES.getProperty("generated.classes.dir");
    private final static String GENERATED_CLASSES_PACKAGE = PROPERTIES.getProperty("generated.classes.package");

    private DynamicAdapterFactory() {}

    public static <T> AdapterFactory<T> createAdapterFactory(Class<T> interfaceType, MethodBuilder methodBuilder) {
        if (interfaceType.isInterface()) {
            // Очищаем директории перед генерацией/компиляцией
            // deleteGeneratedDirectories();

            // 1. Генерируем .java исходники
            String adapterFactorySimpleName = generateAdapterClasses(interfaceType, methodBuilder);
            String fullAdapterFactoryNameClass = GENERATED_CLASSES_PACKAGE + "." + adapterFactorySimpleName;

            // 2. Компилируем сгенерированные .java файлы в .class файлы
            compileGeneratedClasses();

            // 3. Загружаем скомпилированную фабрику и возвращаем ее экземпляр
            return invokeClassAdapter(fullAdapterFactoryNameClass);
        } else {
            throw new IllegalArgumentException("The type must be an interface");
        }
    }

    @SneakyThrows
    private static void deleteGeneratedDirectories() {
        Path sourcesPath = Paths.get(GENERATED_SOURCES_DIR);
        Path classesPath = Paths.get(COMPILED_CLASSES_DIR);

        if (Files.exists(sourcesPath)) {
            FileUtils.deleteDirectory(sourcesPath.toFile());
            log.info("Deleted generated sources directory: {}", sourcesPath.toAbsolutePath());
        }
        if (Files.exists(classesPath)) {
            FileUtils.deleteDirectory(classesPath.toFile());
            log.info("Deleted compiled classes directory: {}", classesPath.toAbsolutePath());
        }
    }

    @SneakyThrows
    private static <T> String generateAdapterClasses(Class<T> interfaceType, MethodBuilder methodBuilder) {
        Files.createDirectories(Paths.get(GENERATED_SOURCES_DIR));

        List<MetaData> metaDataList = MetaDataCollector.collectMetaData(interfaceType);
        JCodeModel codeModel = new JCodeModel();
        String interfaceSimpleName = interfaceType.getSimpleName();
        String adapterNameClass = interfaceSimpleName + "Adapter";
        String adapterFactoryNameClass = interfaceSimpleName + "Factory";

        // === Adapter Class Generation ===
        JDefinedClass adapterClass = codeModel._class(GENERATED_CLASSES_PACKAGE + "." + adapterNameClass);
        adapterClass._implements(interfaceType);

        String fieldName = "obj";
        adapterClass.field(JMod.PRIVATE, Object.class, fieldName);
        JMethod constructorMethod = adapterClass.constructor(JMod.PUBLIC);
        constructorMethod.param(Object.class, fieldName);
        JBlock constructorBlock = constructorMethod.body();
        constructorBlock.assign(JExpr._this().ref(fieldName), JExpr.ref(fieldName));

        for (MetaData metaData : metaDataList) {
            JMethod method = adapterClass.method(JMod.PUBLIC, metaData.getReturnType(), metaData.getMethodName());
            for (Map.Entry<String, Class<?>> entry : metaData.getParameterNameAndType().entrySet()) {
                JClass jclass = codeModel.ref(entry.getValue());
                method.param(jclass, entry.getKey());
            }
            method.annotate(Override.class);
            String methodBody = methodBuilder.getBody(metaData.getMethodName(), interfaceSimpleName, metaData.getReturnType().getName());
            method.body().directStatement(methodBody);
            if (methodBody.contains("IoC.resolve") && !methodBody.contains("IoC.Register")) {
                constructorBlock.directStatement(methodBuilder.getConstructorBody(metaData.getMethodName(), interfaceSimpleName));
            }
        }

        // === AdapterFactory Class Generation ===
        JDefinedClass adapterFactoryClass = codeModel._class(GENERATED_CLASSES_PACKAGE + "." + adapterFactoryNameClass);
        JClass genericInterface = codeModel.ref(interfaceType.getName());
        JClass impl = codeModel.ref(AdapterFactory.class);
        JClass implAndGeneric = impl.narrow(genericInterface); // AdapterFactory<interfaceType>
        adapterFactoryClass._implements(implAndGeneric);

        JMethod method = adapterFactoryClass.method(JMod.PUBLIC, interfaceType, "create");
        method.param(Object.class, "obj"); // Метод create будет принимать Object
        method.annotate(Override.class);
        String body = String.format("return new %s(obj);", adapterNameClass); // Создает экземпляр адаптера
        method.body().directStatement(body);

        // Build: JCodeModel генерирует .java файлы в GENERATED_SOURCES_DIR
        codeModel.build(Paths.get(GENERATED_SOURCES_DIR).toFile());
        log.info("Generated .java files to: {}", Paths.get(GENERATED_SOURCES_DIR).toAbsolutePath());
        return adapterFactoryNameClass; // Возвращаем простое имя фабрики
    }

    @SneakyThrows
    private static void compileGeneratedClasses() {
        // Создаем директорию для скомпилированных классов, если ее нет
        Files.createDirectories(Paths.get(COMPILED_CLASSES_DIR));

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("JDK's JavaCompiler not found. Please run with a JDK, not a JRE.");
        }

        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, null, null);

        // Собираем все .java файлы из директории сгенерированных исходников
        List<File> sourceFiles = Files.walk(Paths.get(GENERATED_SOURCES_DIR))
                .filter(p -> p.toString().endsWith(".java"))
                .map(Path::toFile)
                .collect(Collectors.toList());

        if (sourceFiles.isEmpty()) {
            log.info("No .java files to compile in: {}", GENERATED_SOURCES_DIR);
            return;
        }

        // Опции компиляции:
        // -d COMPILED_CLASSES_DIR: Скомпилированные .class файлы будут помещены сюда
        // -sourcepath GENERATED_SOURCES_DIR: Компилятор будет искать исходники зависимостей здесь
        // -cp ...: Добавляем текущий classpath, чтобы компилятор нашел AdapterFactory, Lombok, JCodeModel и т.д.
        List<String> options = new ArrayList<>();
        options.add("-d"); options.add(COMPILED_CLASSES_DIR);
        options.add("-sourcepath"); options.add(GENERATED_SOURCES_DIR);
        options.add("-cp"); options.add(System.getProperty("java.class.path")); // Обязательно для внешних зависимостей!

        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(sourceFiles);

        JavaCompiler.CompilationTask task = compiler.getTask(
                null, fileManager, diagnostics, options, null, compilationUnits);

        boolean success = task.call();

        for (javax.tools.Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            log.info("Compilation Diagnostic: {}", diagnostic.getMessage(null));
        }

        if (!success) {
            throw new RuntimeException("Dynamic compilation failed!");
        } else {
            log.info("Dynamic compilation successful! .class files in: {}", Paths.get(COMPILED_CLASSES_DIR).toAbsolutePath());
        }
        fileManager.close();
    }

    @SneakyThrows
    private static <T> AdapterFactory<T> invokeClassAdapter(String fullAdapterFactoryNameClass) {
        Path classesDirPath = Paths.get(COMPILED_CLASSES_DIR);
        URL classesDirURL = classesDirPath.toUri().toURL();

        ClassLoader parentClassLoader = Thread.currentThread().getContextClassLoader();

        URLClassLoader urlClassLoader = new URLClassLoader(new URL[]{classesDirURL}, parentClassLoader);

        try {
            loadAllClassesFromPackage(urlClassLoader, GENERATED_CLASSES_PACKAGE);

            Class<?> invokedClass = urlClassLoader.loadClass(fullAdapterFactoryNameClass);
            Constructor<?> constructor = invokedClass.getConstructor();

            AdapterFactory<T> factory = (AdapterFactory<T>) constructor.newInstance();
            return factory;
        } catch (Exception e) {
            urlClassLoader.close();
            throw e;
        }
    }

    @SneakyThrows
    private static void loadAllClassesFromPackage(URLClassLoader classLoader, String packageName) {
        Path classesDirPath = Paths.get(COMPILED_CLASSES_DIR);
        String packagePath = packageName.replace('.', '/');
        Path packageDirPath = classesDirPath.resolve(packagePath);

        if (!Files.exists(packageDirPath)) {
            log.warn("Package directory not found: {}", packageDirPath);
            return;
        }

        List<Path> classFiles = Files.walk(packageDirPath)
                .filter(p -> p.toString().endsWith(".class"))
                .toList();

        for (Path classFile : classFiles) {
            String relativePath = classesDirPath.relativize(classFile).toString();
            String className = relativePath
                    .replace(File.separator, ".")
                    .replace(".class", "");

            try {
                classLoader.loadClass(className);
                log.info("Pre-loaded class: {}", className);
            } catch (ClassNotFoundException e) {
                log.warn("Failed to pre-load class: {}", className, e);
            }
        }
    }

}
