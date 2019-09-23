/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import java.util.ArrayList;
import java.util.Random;

/**
 * класс реализации популяции из особей
 * @author dmitriz
 */
public class Population {
    //совокупность особей популяции
    Genotype[] osobi;
    //стоимость(приспособленость) популяции
    int cost;
    //создание нового поколения из случайных особей 
    public Population(int size, int length, int minmax, int[] x, int[] y){
        osobi=new Genotype[size];
        cost=0;
        Random rand=new Random(System.currentTimeMillis());
        int[] zero=new int[length];
        for(int i=0;i<length;i++)
        {
            zero[i]=0;
        }
        osobi[0]=new Genotype(zero);
        for(int i=1;i<size;i++)
        {
            int[] gens=new int[length];
            for(int j=0;j<length;j++)
            {                
                gens[j]=rand.nextInt(minmax*2)-minmax;
            }
            osobi[i]=new Genotype(gens);
            while(search(osobi[i],osobi,i))
            {
                for(int j=0;j<length;j++)
                {                
                    gens[j]=rand.nextInt(minmax*2)-minmax;
                }
                osobi[i]=new Genotype(gens);
            }
            cost+=osobi[i].cost(x, y);
        }
    }
    //создание нового поколения из лучших особей
    public Population(Genotype[] best,int[] x, int[] y){
        ArrayList<Genotype[]> potomki=new ArrayList<Genotype[]>();
        cost=0;
        for(int i=0;i<best.length-1;i++)
            for(int j=i+1;j<best.length;j++)
            {
                potomki.add(best[i].crossbreedingWith(best[j]));
            }
        osobi=new Genotype[best.length+potomki.get(0).length*potomki.size()];
        for(int i=0;i<best.length;i++)
        {
            osobi[i]=best[i];
            cost+=osobi[i].cost(x, y);
        }
        int ind=best.length;
        for(int i=0;i<potomki.size();i++)
        {
            for(int j=0;j<potomki.get(i).length;j++)
            {
                osobi[ind]=potomki.get(i)[j];
                cost+=osobi[ind++].cost(x, y);
            }
        }
    }
    //универсальный конструктор для использования при наследовании
    //обнуляет суммарную стоимость поколения
    protected Population(){
        cost=0;
    }
    //мутация поколения
    public void mutation(int percent){
        for(int i=0;i<osobi.length;i++)
        {
            osobi[i].mutation(percent);
        }
    }
    //селекция поколения
    public Genotype[] selection(int count,int[] x, int[] y){
        if (count<osobi.length)
        {
            Genotype[] best=new Genotype[count];
            best[0]=osobi[0];
            int j=1;
            for(int i=1;i<count;i++)
            {
                if (!search(osobi[j],best,i))
                {
                    best[i]=osobi[j];
                    int ind=i;
                    while ((best[ind].cost(x, y)/cost)<(best[ind-1].cost(x, y)/cost))
                    {
                        best[ind]=best[ind-1];
                        best[--ind]=osobi[j];
                        if (ind==0) break;
                    }
                }
                else
                {
                    i--;
                }
                j++;
            }           
            for(int i=j;i<osobi.length;i++)
            {
                if (!search(osobi[i],best,best.length))
                {
                    if ((osobi[i].cost(x, y)/cost)<(best[count-1].cost(x, y)/cost))
                    {
                        best[count-1]=osobi[i];
                        int ind=count-1;
                        while ((best[ind].cost(x, y)/cost)<(best[ind-1].cost(x, y)/cost))
                        {
                            best[ind]=best[ind-1];
                            best[--ind]=osobi[i];
                            if (ind==0) break;
                        }
                    }
                }
            }
            return best;
        }
        return osobi;
    }
    //поиск генотипа среди указанноых количества генотипов 
    private boolean search(Genotype target,Genotype[] source, int limit){
        for(int i=0;i<limit;i++)
        {
            int count=target.gens.length;
            for(int j=0;j<target.gens.length;j++)
            {
                if (target.gens[j]==source[i].gens[j])
                {
                    count--;
                }
            }
            if (count==0)
            {
                return true;
            }
        }
        return false;
    }
}
