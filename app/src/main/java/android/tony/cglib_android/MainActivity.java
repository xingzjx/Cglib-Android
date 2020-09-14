package android.tony.cglib_android;

import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.alibaba.fastjson.JSON;
import com.android.tonystark.cglib.proxy.Enhancer;
import com.android.tonystark.cglib.proxy.MethodInterceptor;
import com.android.tonystark.cglib.proxy.MethodProxy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.hjq.permissions.OnPermission;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private String TAG = "DemoPrinter";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);

        if (!XXPermissions.isHasPermission(this, Permission.Group.STORAGE)) {
            // XXPermissions.gotoPermissionSettings(this)
            XXPermissions.with(this)
                    .permission(Permission.Group.STORAGE)
                    .request(new OnPermission() {
                        @Override
                        public void hasPermission(List<String> granted, boolean isAll) {

                        }

                        @Override
                        public void noPermission(List<String> denied, boolean quick) {

                        }
                    });
        }


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "请查看日志", Snackbar.LENGTH_LONG).show();
                // test();

                // toInstall("/sdcard/target.apk");
                test2("/sdcard/target.apk");
            }
        });

    }

    private void test2(String apkPath) {

        try {
            PackageManager packageManager = getPackageManager();
            Class pmClz = packageManager.getClass();
            Class aClass = Class.forName("android.app.PackageInstallObserver");
            Enhancer enhancer = new Enhancer(this);
            enhancer.setSuperclass(aClass);
            enhancer.setInterceptor(new MethodInterceptor() {
                @Override
                public Object intercept(Object object, Object[] args, MethodProxy methodProxy) throws Exception {
                    Log.d(TAG, "执行拦截器之前：" + methodProxy.getMethodName());
                    Object result = methodProxy.invokeSuper(object, args);
                    Log.d(TAG, "执行拦截器之后  " + methodProxy.getMethodName() + " result:" + JSON.toJSONString(args));
                    return result;
                }
            });
            Method method = pmClz.getDeclaredMethod("installPackage", Uri.class, aClass, int.class, String.class);
            method.setAccessible(true);
            method.invoke(packageManager, Uri.fromFile(new File(apkPath)), enhancer.create(), 2, null);
            Log.d(TAG, "成功！！！");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "异常！！！");
        }

    }

    private void toInstall(String apkPath) {
        try {
            PackageManager packageManager = getPackageManager();
            Class pmClz = packageManager.getClass();
            Class aClass = Class.forName("android.app.PackageInstallObserver");
            Constructor constructor = aClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            Object installObserver = constructor.newInstance();
            Method method = pmClz.getDeclaredMethod("installPackage", Uri.class, aClass, int.class, String.class);
            method.setAccessible(true);
            method.invoke(packageManager, Uri.fromFile(new File(apkPath)), installObserver, 2, null);
            Log.d(TAG, "成功！！！");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "异常！！！");
        }

    }

    private void test() {
        Enhancer enhancer = new Enhancer(this);
        enhancer.setSuperclass(DemoPrinter.class);
        enhancer.setInterceptor(new MethodInterceptor() {
            @Override
            public Object intercept(Object object, Object[] args, MethodProxy methodProxy) throws Exception {
                Log.d(TAG, "执行拦截器的方法");
                return methodProxy.invokeSuper(object, args);
            }
        });
        final DemoPrinter demoPrinterProxy = (DemoPrinter) enhancer.create();
        demoPrinterProxy.print2();
    }


    public class DemoPrinter {// 拦截的目标类

        public void print() {
            Log.i(DemoPrinter.class.getSimpleName(), "print() call");
        }

        public int print2() {
            Log.i(DemoPrinter.class.getSimpleName(), "print2() call");
            return 5;
        }

        public void throwException() {
            throw new RuntimeException("测试抛出异常");
        }
    }
}
