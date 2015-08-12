package at.yawk.wm.dbus;

import org.testng.annotations.Test;

import static org.testng.Assert.*;

/**
 * @author yawkat
 */
public class DbusTest {
    @Test(enabled = false)
    public void test() throws Exception {
        Power power = new Dbus().power();
        System.out.println(power.getTimeToEmpty());
        System.out.println(power.getTimeToFull());
    }
}