package pl.hws.phptester;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Context {
    private final static Context INSTANCE = new Context();

    private Context() {
        
    }

    public static Context getInstance() {
        return INSTANCE;
    }

    public static SimpleDateFormat getSDFInstance() {
        return new SimpleDateFormat("YYYY-MM-dd HH:mm:ss");
    }

    public void showError(Throwable ex) {
        String message = ex.getMessage();

        if (message == null || message.isEmpty()) {
            if (ex.getCause() != null) {
                showError(ex.getCause());
            } else {
                showError("unknown exception");
            }
        } else {
            showError(message);
        }

        ex.printStackTrace();
    }

    public void showError(String message) {
        this.showError(message, false);
    }

    public void showError(String message, Boolean critical) {
        System.err.println(getPrefix() + ": **ERR** " + message);

        if (critical) {
            throw new RuntimeException(message);
        }
    }

    public void showMessage(String message) {
        System.out.println(getPrefix() + ": " + message);
    }

    private String getPrefix() {
        return getSDFInstance().format(new Date()) + " " + Thread.currentThread().getName() + ": ";
    }
}