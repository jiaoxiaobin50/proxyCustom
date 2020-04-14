package com.luban.proxy;

import com.sun.jndi.toolkit.url.UrlUtil;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

public class ProxyUtil {

    /**
     *  content --->string
     *  .java  io
     * .class
     *
     *
     *
     * .new   反射----》class
     * @return
     */
    public static Object newInstance(Object target){

        // 1.编写代理对象的Java类合成一个String content字符串形式的
          // 1.1 java 文件的包和import导入
          // 1.2 类文件的类名第一行
          // 1.3 实例变量private  infName target;
          // 1.4 方法的循环拼接。以及声明返回值等
          // 1.5 参数的获取，以及拼接入参格式
          // 1.6 代理部分System.out.println
          // 1.7 最后将一整个方法拼接成完整的方法
          // 1.8 拼接所有的文件内容成为一个Java文件的字符串格式

        // 2.创建一个Java文件
        // 3.将content通过FileWriter将content写入到文件中，生成Java文件
        // 4.通过第三方编译器将Java文件编译成class文件
        // 5.通过URLClassLoader类加载器加载到jvm中
        // 6.通过反射方法实例化代理对象（调用clazz的构造方法）
        Object proxy=null;
        Class targetInf = target.getClass().getInterfaces()[0];
        Method methods[] =targetInf.getDeclaredMethods();
        String line="\n";
        String tab ="\t";
        String infName = targetInf.getSimpleName();
        String content ="";
        String packageContent = "package com.google;"+line;
        String importContent = "import "+targetInf.getName()+";"+line;
        String clazzFirstLineContent = "public class $Proxy implements "+infName+"{"+line;
        String filedContent  =tab+"private "+infName+" target;"+line;
        String constructorContent =tab+"public $Proxy ("+infName+" target){" +line
                                  +tab+tab+"this.target =target;"
                                  +line+tab+"}"+line;
        String methodContent = "";
        for (Method method : methods) {
            String returnTypeName = method.getReturnType().getSimpleName();
            String methodName =method.getName();
            // Sting.class String.class
            Class args[] = method.getParameterTypes();
            String argsContent = "";
            String paramsContent="";
            int flag =0;
            for (Class arg : args) {
                String temp = arg.getSimpleName();
                //String
                //String p0,Sting p1,
                argsContent+=temp+" p"+flag+",";
                paramsContent+="p"+flag+",";
                flag++;
            }
            if (argsContent.length()>0){
                argsContent=argsContent.substring(0,argsContent.lastIndexOf(",")-1);
                paramsContent=paramsContent.substring(0,paramsContent.lastIndexOf(",")-1);
            }

            methodContent+=tab+"public "+returnTypeName+" "+methodName+"("+argsContent+") {"+line
                          +tab+tab+"System.out.println(\"log\");"+line
                          +tab+tab+"target."+methodName+"("+paramsContent+");"+line
                          +tab+"}"+line;

        }

        content=packageContent+importContent+clazzFirstLineContent+filedContent+constructorContent+methodContent+"}";

        File file =new File("d:\\com\\google\\$Proxy.java");
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file);
            fw.write(content);
            fw.flush();
            fw.close();


            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

            StandardJavaFileManager fileMgr = compiler.getStandardFileManager(null, null, null);
            Iterable units = fileMgr.getJavaFileObjects(file);

            JavaCompiler.CompilationTask t = compiler.getTask(null, fileMgr, null, null, null, units);
            t.call();
            fileMgr.close();

            URL[] urls = new URL[]{new URL("file:D:\\\\")};
            URLClassLoader urlClassLoader = new URLClassLoader(urls);
            Class clazz = urlClassLoader.loadClass("com.google.$Proxy");

            Constructor constructor = clazz.getConstructor(targetInf);

            proxy = constructor.newInstance(target);
            //clazz.newInstance();
            //Class.forName()
        }catch (Exception e){
            e.printStackTrace();
        }





        /**
         * public UserDaoLog(UserDao target){
         * 		this.target =target;
         *
         *        }
         */
        return proxy;
    }
}
