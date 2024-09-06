import com.ass1.Server.*;

public class ServerSimulator {
    public static void main(String[] args) {
        
        /*
         * Create 5 instances of server class in different address and ports
         */

         Server[] servers = new Server[5];
         
         for (int i = 0; i < 5; i++) {
            servers[i] = new Server();
         }
    }
}
