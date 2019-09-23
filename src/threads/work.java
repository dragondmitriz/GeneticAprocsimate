/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package threads;

import classes.Genotype;
import classes.Population;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JOptionPane;
import static main.window.canvas1;
import static main.window.jLabel3;

/**
 * класс для вычисления генетическим алгоритмом с использованием мощностей основного приложения
 * @author dmitriz
 */
public class work implements Runnable{
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
    
    public work(int pS,int gL,int bS, int mm, int mP,int[] X,int[] Y){
        populationSize=pS;
        genotypeLength=gL;
        bestSize=bS;
        minmax=mm;
        mutationPersent=mP;
        x=X;
        y=Y;
    }
    
    @Override
    public void run()
    {      
        main.window.isWork=true;
        int indPopulation=1;
        jLabel3.setText("Расчёт "+Integer.toString(indPopulation++)+"-ой популяции...");
        Population first=new Population(populationSize,genotypeLength,minmax,x,y);
        Genotype[] best=first.selection(bestSize, x, y);
        while(best[0].cost(x, y)>x.length*genotypeLength)
        {
            jLabel3.setText("f(x)="+best[0].show());
            jLabel3.setText("Расчёт "+Integer.toString(indPopulation++)+"-ой популяции...");
            Population next=new Population(best,x,y);
            next.mutation(mutationPersent);
            best=next.selection(bestSize, x, y);
        }
        //отрисовка результата
        jLabel3.setText("f(x)="+best[0].show());
        Graphics g=canvas1.getGraphics();
        g.clearRect(0, 0, canvas1.getWidth(), canvas1.getHeight());
        g.setColor(Color.BLACK);
        int width=canvas1.getWidth();
        int height=canvas1.getHeight();
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
            double Y=best[0].value(X);
            valueY[i]=Y;
            if (i>1)
            {
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
        if (maxx-minx>30)
        {
            d=10;
        } else if (maxx-minx>300)
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
        while(x0<=maxx)
        {
            g.drawLine((int)((x0-minx)/kx),(int)axisX-3,(int)((x0-minx)/kx),(int)axisX+3);
            g.drawString(Integer.toString(x0), (int)((x0-minx)/kx), (int)axisX+15);
            x0+=d;
        }
        d=1;
        if (maxy-miny>30)
        {
            d=10;
        } else if (maxy-miny>300)
        {
            d=100;
        }
        while(y0<=maxy)
        {
            g.drawLine((int)axisY-3,(int)((maxy-y0)/ky),(int)axisY+3,(int)((maxy-y0)/ky));
            if (y0!=0) g.drawString(Integer.toString(y0), (int)axisY+5, (int)((maxy-y0)/ky)+15);
            y0+=d;
        }
        //отрисовка графика
        X=minx;
        double preX=X;
        double preY=best[0].value(preX);
        X+=kx;
        while(X<=maxx)
        {
            double Y=best[0].value(X);
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
        main.window.isWork=false;
    }
}
