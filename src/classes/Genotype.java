package classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

/**
 * класс реализующий генотип
 * @author dmitriz
 */
public class Genotype implements Serializable{
    //набор генов
    int[] gens;
    //инициализация генотипа
    public Genotype(int[] input) {
        gens = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            gens[i] = input[i];
        }
    }
    //мутация генотипа
    public void mutation(int percent) {
        Random rand = new Random(System.currentTimeMillis());
        for(int i=0;i<gens.length;i++)
        {
            if (rand.nextInt(100)>percent)
            {
                gens[i]+=Math.pow(-1, rand.nextInt(2));
            }
        }
    }
    //"стоимость" генотипа в отношении к оригиналу, введёном пользователем (МНК)
    public double cost(int[] x,int[] y){
        double cost=0;
        for(int i=0;i<x.length;i++)
        {
            double f=0;
            for(int j=0;j<gens.length;j++)
            {
                f+=gens[j]*Math.pow(x[i],j);
            }
            cost+=Math.pow(y[i]-f,2);
        }
        return cost;
    }
    //скрещивание генотипа с указанным генотипом
    public Genotype[] crossbreedingWith(Genotype pair){
        //генерируем коллекция потомков с помощью рекурсивного скрещивания
        ArrayList<Genotype> list=recursiveCrossbreeding(this,pair,gens.length-1);
        //создаём массив потомков
        Genotype[] potomki=new Genotype[list.size()-1];
        //переносим потомков из коллекции в массив
        for(int i=0;i<potomki.length;i++)
        {
            potomki[i]=list.get(i);
        }
        //возвращаем массив потомков
        return potomki;
    }
    //рекурсия для генерации потомков в процессе скрещивания
    protected ArrayList<Genotype> recursiveCrossbreeding(Genotype current, Genotype pair,int i){
        ArrayList<Genotype> list=new ArrayList<Genotype>();     
        Genotype second=new Genotype(current.gens);
        second.gens[i]=current.gens[i];
        list.add(second);               
        if (i>0)
        {
                list.addAll(recursiveCrossbreeding(second,pair,i-1));
                list.addAll(recursiveCrossbreeding(current,pair,i-1));
        }
        return list;
    }
    //строковое представление генотипа в виде аппроксимирующей функции
    public String show(){
        String rezult="";
        if ((gens.length>2)&&(gens[gens.length-1]!=0))
        {
            rezult+=Integer.toString(gens[gens.length-1])+"x^"+Integer.toString(gens.length-1);
        }
        for(int i=gens.length-2;i>1;i--)
        {
            if (gens[i]!=0)
            {
                if (gens[i]>0)
                {
                    rezult+="+";
                }
                rezult+=Integer.toString(gens[i])+"x^"+Integer.toString(i);               
            }
        }
        if (gens[1]!=0)
        {
            if (gens[1]>0)
            {
                rezult+="+";
            }
            rezult+=Integer.toString(gens[1])+"x";            
        }
        if (gens[0]!=0)
        {
            if (gens[0]>0)
            {
                rezult+="+";
            }
            rezult+=Integer.toString(gens[0]);
        }
        if (rezult.equals(""))
        {
            rezult="0";
        }
        else if (rezult.substring(0, 1).equals("+"))
        {
            rezult=rezult.substring(1);
        }
        return rezult;
    }
    //значение функции при указананном x
    public double value(double x){
        double rezult=0;
        for(int i=0;i<gens.length;i++)
        {
            rezult+=gens[i]*Math.pow(x,i);
        }
        return rezult;
    }
}
