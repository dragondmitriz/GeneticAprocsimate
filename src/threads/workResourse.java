/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import classes.Genotype;
import classes.PackGenotype;
import classes.Population;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import main.window;

/**
 * поток работы приложения как ресурса для административного приложения в сети
 * @author dmitriz
 */
public class workResourse implements Runnable{
    //функция вывода информации о процессе работы ресурса на форму приложения
    void log(String message){
        if (window.jTextArea1.getText().length()>400)
        {
            try
            {
                Thread.sleep(300);
            }catch(InterruptedException IE){}
            window.jTextArea1.setText("");
        }
        window.jTextArea1.setText(window.jTextArea1.getText()+message+"\n");
    }
    
    @Override
    public void run() {
        main.window.isWork=true;
        log("начало работы потока");
        try{
            while(true)
            {
                byte[] data=new byte[60];
                DatagramSocket ds=new DatagramSocket(7111);
                log("создан сокета");
                DatagramPacket dp=new DatagramPacket(data,data.length);
                log("создан пакета");
                ds.receive(dp);
                log("ожидание сообщения");
                ds.close();
                log("принятие пакета сокета");
                String message=new String(data);
                log("создание строки из пакета. Результат: "+message);
                if (!message.substring(0, 12).equals("aprocsimate?")) 
                {
                    log("строка не корректна");
                    continue;
                }
                log("строка корректна");
                String ip=message.substring(12, message.indexOf("|"));
                log("извлечение из строки ip ресурса "+ip);
                String ipAdmin=message.substring(message.indexOf("|")+1);
                log("извлечение из строки ip админа "+ipAdmin);
                message="yes"+ip;
                data=message.getBytes();
                log("сообщение для отправки ответа админу собрано: "+ new String(data));
                ds=new DatagramSocket();
                log("создан сокет");
                dp=new DatagramPacket(data,data.length,InetAddress.getByName(ipAdmin),7111);
                log("создан пакет - "+ new String(data));
                ds.send(dp);
                log("пакет отправлен "+new String(dp.getData()));
                ds.close();
                log("сокет закрыт");
                break;
            }
            log("выход из цикла");
            ServerSocket ss=new ServerSocket(7111);
            log("создан серверный сокет");
            Socket adminSocket=ss.accept();
            log("принято сообщение от админа");
            ObjectOutputStream out=new ObjectOutputStream(adminSocket.getOutputStream());
            log("создан поток отправки");
            ObjectInputStream in=new ObjectInputStream(adminSocket.getInputStream());
            log("создан пакет считывания");
            while(main.window.isWork)
            {
                log("вход в цикл работы ресурса с адиином");
                try
                {
                    boolean first=(boolean)in.readBoolean();
                    log("принято: "+first);
                    int[] firstparameters=null;
                    Genotype[] best=null;
                    log("инитиализированы массивы для входящих параметров генетического алгоритма");
                    if (first)
                    {
                        log("считывание входящих параметров для первого поколения");
                        firstparameters=(int[])in.readObject();
                        log("принято:");
                        for(int i=0;i<firstparameters.length;i++)
                        {
                            log(Integer.toString(firstparameters[i]));
                        }
                    }
                    else
                    {
                        log("считывание лучших особей для создания нового поколения");
                        best=(Genotype[])in.readObject();
                        log("принято:");
                        for(int i=0;i<best.length;i++)
                        {
                            log(best[i].show());
                        }
                    }
                    int[] parameters=(int[])in.readObject();
                    log("принято:");
                    for(int i=0;i<parameters.length;i++)
                    {
                        log(Integer.toString(parameters[i]));
                    }
                    int[] x=(int[])in.readObject();
                    log("принято: x");
                    int[] y=(int[])in.readObject();
                    log("принято: y");
                    if (first)
                    {
                        log("начало создания первой популяции");
                        Population population=new Population(firstparameters[0],firstparameters[1],firstparameters[2],x,y);
                        log("первая популяция создана");
                        PackGenotype pack=new PackGenotype(population.selection(parameters[0], x, y));
                        log("собран пакет генотипов селекциик для отправки: ");
                        for(int i=0;i<pack.get().length;i++)
                        {
                            log(pack.get()[i].show());
                        }
                        out.writeObject(pack);
                        log("пакет отправлен");
                    }
                    else
                    {
                        log("создание новой популяции");
                        Population population=new Population(best,x,y);
                        log("популяция создана");
                        population.mutation(parameters[1]);
                        log("популяция мутировала");
                        PackGenotype pack=new PackGenotype(population.selection(parameters[0], x, y));
                        log("собран пакет генотипов селекциик для отправки");
                        out.writeObject(pack);
                        log("пакет отправлен");
                    }
                }catch(Exception e)
                {
                    log("ошибка - прерывание цикла: "+e.getMessage());
                    main.window.isWork=false;
                    log("выход из цикла работы ресурса");
                } 
            }
            log("закрытие потоков");
            in.close();
            log("входящий поток закрыт");
            out.close();
            log("выходящий поток закрыт");
            adminSocket.close();
            ss.close();
        }catch(IOException IOE)
        {
            log("ошибка работы потока: "+IOE.toString());
        }
        log("создание нового потока ресурса");
        Thread workResourse=new Thread(new workResourse());
        log("поток ресурса создан");
        workResourse.start();
        log("новый поток запущен и работа старого закончена");
    }
}
