package home.blockova;

import org.hsqldb.Server;

public class SpustacDatabazy {
    
    public static void  execute(){
        Server server =new Server();
        server.setDatabaseName(0,"blockovadb");
        server.setDatabasePath(0 ,"db/blockovadb");
        server.start();
          
    }
    public static void main(String[] args) {
        Server server =new Server();
        server.setDatabaseName(0,"blockovadb");
        server.setDatabasePath(0 ,"db/blockovadb");
        server.start();
    }
}
