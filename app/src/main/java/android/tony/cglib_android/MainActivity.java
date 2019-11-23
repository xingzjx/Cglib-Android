package android.tony.cglib_android;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.android.tonystark.cglib.proxy.Enhancer;
import com.android.tonystark.cglib.proxy.MethodInterceptor;
import com.android.tonystark.cglib.proxy.MethodProxy;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.lang.reflect.InvocationTargetException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);

        //Demo begin
        DemoPrinter printer = new DemoPrinter();
        final DemoPrinter printerProxy = new DemoPrinterProxy(this, printer).getProxy();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printerProxy.print();
                Log.i(DemoPrinter.class.getSimpleName(), "print2 return: " + printerProxy.print2());
                printerProxy.throwException();
                Snackbar.make(view, "请查看日志", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private class DemoPrinterProxy extends DemoPrinter implements MethodInterceptor {

        private DemoPrinter mDemoPrinter;

        private Context mContext;

        public DemoPrinterProxy(Context context, DemoPrinter demoPrinter) {
            mDemoPrinter = demoPrinter;
            mContext = context;
        }

        public DemoPrinter getProxy() {
            Enhancer enhancer = new Enhancer(mContext);
            enhancer.setSuperclass(DemoPrinter.class);
            enhancer.setInterceptor(this);
            return (DemoPrinter) enhancer.create();
        }

        @Override
        public Object intercept(Object object, Object[] args, MethodProxy methodProxy) throws Exception {
            Log.i(DemoPrinter.class.getSimpleName(), "before " + methodProxy.getMethodName() + " called");
            Object result = null;
            try {
                result = methodProxy.invokeSuper(mDemoPrinter, args);
            } catch (InvocationTargetException e) {
                Log.i(DemoPrinter.class.getSimpleName(), "cache " + methodProxy.getMethodName() + " exception:" + e.getTargetException().getMessage());
            }
            Log.i(DemoPrinter.class.getSimpleName(), "after " + methodProxy.getMethodName() + " called");
            return result;
        }
    }

    public class DemoPrinter {

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
