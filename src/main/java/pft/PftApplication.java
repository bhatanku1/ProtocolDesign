package pft;

/**
 * Created by rabbiddog on 5/15/16.
 */
public class PftApplication {

    public static void main(String[] arg)
    {
        (new Thread(new Server(7000, 20))).start();
    }
}
