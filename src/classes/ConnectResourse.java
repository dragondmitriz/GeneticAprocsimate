/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

/**
 * класс для хранения соединения с приложением-ресурсом
 * @author dmitriz
 */
public class ConnectResourse {
    //хранение в классе сокета
    private Socket s;
    //открытый доступ к потокам обмена информацией
    public ObjectInputStream in;
    public ObjectOutputStream out;
    //создание соединение и потоков ввода и вывода объектов
    public ConnectResourse(InetAddress addr) throws IOException{
        s=new Socket(addr,7111);
        in=new ObjectInputStream(s.getInputStream());
        out=new ObjectOutputStream(s.getOutputStream());
    }
    //закрытие соединения
    public void close() throws IOException{
        s.close();
    }
}
