/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import classes.ConnectResourse;
import classes.Genotype;
import classes.PackGenotype;
import java.awt.Color;
import java.awt.Graphics;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import javax.swing.JOptionPane;
import static main.window.canvas1;
import static main.window.jLabel3;

/**
 * административный поток для распараллеливвания популяции
 * @author dmitriz
 */
public class workAdmin implements Runnable{
    //список адресов вычислительных ресурсов в сети
    ArrayList<InetAddress> addresses;
    //список сокетов подключения к ресурсам в сети
    ArrayList<ConnectResourse> resourses;
    //коллекция массивов лучших особей подпопуляций
    ArrayList<Genotype[]> bests;
    
    //количество особей начальной пупуляции
    protected int populationSize;
    //длина генотипа
    protected int genotypeLength;
    //количество особей для селекции
    protected int bestSize;
    //максимальное значение гена в генотипе
    protected int minmax;
    //процент вероятности мутации гена в генотипе
    protected int mutationPersent;
    //массив значений х
    int[] x;
    //массив значений у
    int[] y;
    
    public workAdmin(int pS,int gL,int bS, int mm,int mP,int[] X,int[] Y){
        populationSize=pS;
        genotypeLength=gL;
        bestSize=bS;
        minmax=mm;
        mutationPersent=mP;
        x=X;
        y=Y;
    }
    
    @Override
    public void run(){
        main.window.isWork=true;
        //сканируем сеть
        scan();
        //определяем вариант вычислений в зависимости от наличия приложений-ресурсов
        if (addresses.size()>0)
        {
            //подключаемся к приложениям-ресурсам
            resourses=new ArrayList<>();
            connect();
            bests=new ArrayList<>();
            //начало генетического алгоритма со стороны основного приложения
            //вывод информации о процессе вычислений
            int indPopulation=1;
            jLabel3.setText("Расчёт "+Integer.toString(indPopulation++)+"-ой популяции...");
            //создание диалогов с запросами на первую популяцию
            for(int i=0;i<resourses.size();i++)
            {
                Thread dialogResourse=new Thread(new DialogResourse(i,populationSize/resourses.size(),genotypeLength,minmax,bestSize/resourses.size(),mutationPersent,x,y));
                dialogResourse.start();
            }
            //ожидание результатов селекций
            while(bests.size()<resourses.size())
            {
                try
                {
                Thread.sleep(100);
                } catch(InterruptedException IE){}
            }
            //проверка "качества" лучшей особи
            while(bests.get(indBestOfBests())[0].cost(x,y)>genotypeLength*x.length/2)
            {
                //вывод информации о процессе вычислений
                jLabel3.setText("Расчёт "+Integer.toString(indPopulation++)+"-ой популяции...");
                //перемешивание лучших ососбей
                ArrayList<Genotype[]> newbests=new ArrayList<>();
                for(int i=0;i<bests.size();i++)
                {
                    Genotype[] newBest=new Genotype[bestSize/resourses.size()];
                    int ind=i;
                    for(int j=0;j<bestSize/resourses.size();j++)
                    {
                        newBest[j]=bests.get(ind++)[j];
                        if (ind==bests.size())
                        {
                            ind=0;
                        }
                    }
                    newbests.add(newBest);
                } 
                bests=new ArrayList<>();
                //распределение лучших особей по приложениям-ресурсам
                for(int i=0;i<resourses.size();i++)
                {
                    Thread dialogResourse=new Thread(new DialogResourse(i,newbests.get(i),bestSize/resourses.size(),mutationPersent,x,y));
                    dialogResourse.start();
                }
                //ожидание результатов селекции
                while(bests.size()<resourses.size())
                {
                    try
                    {
                    Thread.sleep(100);
                    } catch(InterruptedException IE){}
                }
            }
            //разрыв соединения с приложениями-ресурсами
            for (ConnectResourse resourse : resourses) {
                try {
                    resourse.out.writeObject(null);
                    resourse.close();
                }catch(IOException IOE)
                {
                    //JOptionPane.showMessageDialog(main.window.jLabel3,IOE.toString());
                }
            }
            Genotype bestOfTheBest=bests.get(indBestOfBests())[0];
            //отрисовка результата
            jLabel3.setText("f(x)="+bestOfTheBest.show());
            Graphics g=canvas1.getGraphics();
            g.clearRect(0, 0, canvas1.getWidth(), canvas1.getHeight());
            g.setColor(Color.BLACK);
            int width=canvas1.getWidth();
            int height=canvas1.getHeight();
            //нахождние границ значений по OX и OY
            int minx,maxx,miny,maxy;
            minx=maxx=miny=maxy=x[0];
            for(int i=1;i<x.length;i++)
            {
                if (x[i]<minx)
                {
                    minx=x[i];
                }
                if (x[i]>maxx)
                {
                    maxx=x[i];
                }
                if (y[i]<miny)
                {
                    miny=y[i];
                }
                if (y[i]>maxy)
                {
                    maxy=y[i];
                }
            }    
            minx-=1;
            maxx+=1;
            double kx=Math.abs((double)(maxx-minx)/(double)width);
            //поиск значений по OY
            double[] valueY=new double[(int)((maxx-minx)/kx)];
            double[] valueX=new double[(int)((maxx-minx)/kx)];
            double X=minx;
            for(int i=0;i<valueY.length;i++)
            {
                valueX[i]=X;
                double Y=bestOfTheBest.value(X);
                valueY[i]=Y;
                if (i>1)
                {
                    //поиск экстремумов апроксимирующей функции в пределах указанных пользователем точек
                    if (((valueY[i-2]<valueY[i-1])&&(valueY[i-1]>valueY[i]))&&(valueY[i-1]>maxy))
                    {
                        maxy=(int) valueY[i-1];
                    }
                    if (((valueY[i-2]>valueY[i-1])&&(valueY[i-1]<valueY[i]))&&(valueY[i-1]<miny))
                    {
                        miny=(int) valueY[i-1];
                    }
                }
                X+=kx;
            }
            miny-=1;
            maxy+=1;
            //сбалансирование диапазонов значений
            if ((maxx-minx)>(maxy-miny))
            {
                int delta=maxx-minx-maxy+miny;
                maxy+=delta/2;
                miny-=delta/2;
            }
            if ((maxx-minx)<(maxy-miny))
            {
                int delta=maxy-miny-maxx+minx;
                maxx+=delta/2;
                minx-=delta/2;
            }
            kx=Math.abs((double)(maxx-minx)/(double)width);
            double ky=Math.abs((double)(maxy-miny)/(double)height);
            //нахождение и отрисовка осей
            double axisY=Math.abs(minx/kx);
            double axisX=Math.abs(maxy/ky);
            g.drawLine(0, (int)axisX, width, (int)axisX);
            g.drawLine((int)axisY, 0, (int)axisY, height);
            int x0=minx;
            int d=1;
            if (maxx-minx>50)
            {
                d=10;
            } else if (maxx-minx>500)
            {
                d=100;
            }
            while(x0<=maxx)
            {
                g.drawLine((int)((x0-minx)/kx),(int)axisX-3,(int)((x0-minx)/kx),(int)axisX+3);
                g.drawString(Integer.toString(x0), (int)((x0-minx)/kx), (int)axisX+15);
                x0+=d;
            }
            int y0=miny;
            while(y0<=maxy)
            {
                g.drawLine((int)axisY-3,(int)((maxy-y0)/ky),(int)axisY+3,(int)((maxy-y0)/ky));
                if (y0!=0) g.drawString(Integer.toString(y0), (int)axisY+5, (int)((maxy-y0)/ky)+15);
                y0++;
            }
            //отрисовка графика
            X=minx;
            double preX=X;
            double preY=bestOfTheBest.value(preX);
            X+=kx;
            while(X<=maxx)
            {
                double Y=bestOfTheBest.value(X);
                g.drawLine((int)((preX-minx)/kx), (int)((maxy-preY)/ky), (int)((X-minx)/kx), (int)((maxy-Y)/ky));
                preX=X;
                preY=Y;
                X+=kx;
            }
            //нанесение указанных пользователем точек
            g.setColor(Color.red);
            for(int i=0;i<x.length;i++)
            {
                g.drawRect((int)((x[i]-minx)/kx)-1, (int)((maxy-y[i])/ky)-1, 2, 2);
            }
        }
        else
        {
            Thread work=new Thread(new work(populationSize,genotypeLength,bestSize,minmax, mutationPersent,x,y));
            work.start();
        }
        main.window.isWork=false;
    }
    
    //нахождение стоимости лучшей особи в коллекции с лучшими особями
    int indBestOfBests(){
        int rezult=0;
        double cost=bests.get(rezult)[0].cost(x, y);
        for(int i=1;i<bests.size();i++)
        {
            double costI=bests.get(i)[0].cost(x, y);
            if (cost>costI)
            {
                cost=costI;
                rezult=i;
            }
        }
        return rezult;
    }
    //сканирование сети на наличие вычислительныъ ресурсов
    protected void scan(){
        addresses=new ArrayList<InetAddress>();        
        //поток принятия сообщений о наличии от вычислительных ресурсов
        inputMessagesScan inputThread=new inputMessagesScan();
        inputThread.start();
        //отпарвка на все возможные компьютеры подключённых к сети, что и админ в области 192.168... по UDP протоколу
        try
        {
            InetAddress[] myIPs=Inet4Address.getAllByName(InetAddress.getLocalHost().getHostName());
            DatagramSocket ds=new DatagramSocket();
            for(InetAddress ia:myIPs)
            {                
                for(int i=1;i<255;i++)
                {
                    try 
                    {
                        String myIP=ia.getHostAddress();
                        String ip=myIP.substring(0,myIP.lastIndexOf(".")+1)+Integer.toString(i);
                        if (!ip.equals(myIP))
                        {
                            String message="aprocsimate?"+ip+"|"+myIP;
                            byte[] data=message.getBytes();
                            
                            InetAddress addr=InetAddress.getByName(ip);
                            DatagramPacket dp=new DatagramPacket(data,data.length,addr,7111);
                            ds.send(dp);
                            
                        }
                    } 
                    catch (IOException IOE)
                    {
                        JOptionPane.showMessageDialog(main.window.jLabel3, IOE.toString());
                    }
                }
            }
            ds.close();
        }catch(Exception E)
        {
            //ошибка поиска текущих подкючений
        }
        inputThread.workstop();
    }
    //соединениt с ресурсами для создания первого поколения
    protected boolean connect(){        
        try
        {
            for(int i=0;i<addresses.size();i++)
            {
                resourses.add(new ConnectResourse(addresses.get(i)));
            }
        }
        catch(Exception e)
        {
            return false;
        }
        return true;
    }
    
    //поток приёма сообщений о наличии от вычислительных ресурсов
    protected class inputMessagesScan extends Thread{
        public boolean work=true;
        DatagramSocket ds; 
        
        @Override
        public void run()
        {
            try
            {
                ds=new DatagramSocket(7111);                               
                while(work)
                {  
                    byte[] data=new byte[40];
                    DatagramPacket dp=new DatagramPacket(data,data.length);
                    ds.receive(dp);
                    String message=new String(data);
                    if (message.substring(0, 3).equals("yes"))
                    {
                        addresses.add(InetAddress.getByName(message.substring(3)));
                    }
                }
            }
            catch(Exception e)
            {
                //JOptionPane.showMessageDialog(main.window.jLabel3, e.toString());
            }
        }
        
        public void workstop()
        {
            work=false;
            ds.close();
        }      
    }
    //поток для обмена сообщениями с ресурсами
    public class DialogResourse implements Runnable{
        boolean first;
        int size,length,minmax;
        int[] x,y;
        Genotype[] best;
        int indRes;
        int bestsize;
        int mutationpercent;
        
        public DialogResourse(int res, int Size, int Length, int MinMax, int BestSize, int MutationPercent, int[] X, int[] Y){
            first=true;
            size=Size;
            length=Length;
            minmax=MinMax;
            x=X;
            y=Y;
            indRes=res;
            bestsize=BestSize;
            mutationpercent=MutationPercent;
        }
        public DialogResourse(int res, Genotype[] Best, int BestSize, int MutationPercent, int[] X, int[] Y){
            first=false;
            best=Best;
            x=X;
            y=Y;
            indRes=res;
            bestsize=BestSize;
            mutationpercent=MutationPercent;
        }
        @Override
        public void run(){
            try
            {
                if (first)
                {
                    resourses.get(indRes).out.writeBoolean(true);
                    int[] message={size,length,minmax};
                    resourses.get(indRes).out.writeObject(message);    
                }
                else
                {
                    resourses.get(indRes).out.writeBoolean(false);
                    resourses.get(indRes).out.writeObject(best);
                }
                int[] message={bestsize,mutationpercent};
                resourses.get(indRes).out.writeObject(message);
                resourses.get(indRes).out.writeObject(x);
                resourses.get(indRes).out.writeObject(y);
                PackGenotype inPack=(PackGenotype)resourses.get(indRes).in.readObject();
                bests.add(inPack.get());
            } catch(IOException IOE)
            {
                JOptionPane.showMessageDialog(main.window.jLabel3, IOE.toString());
            } catch(ClassNotFoundException CNFE)
            {
                int qwe=0;
            }
        }
    }
}
