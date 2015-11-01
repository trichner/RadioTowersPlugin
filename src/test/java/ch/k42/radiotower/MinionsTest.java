package ch.k42.radiotower;

import org.junit.Test;

/**
 * Created on 15.02.2015.
 *
 * @author Thomas
 */
public class MinionsTest {

    @Test
    public void obfuscation() {
        String obfs, msg = "Hello World!!!111";

        for(double scale = 0;scale<1.5;scale+=0.1){
            obfs = Minions.obfuscateMessage(msg,scale);
            System.out.printf("%f : %s\n", scale, obfs);
        }
    }
}
