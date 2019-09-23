/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package classes;

import java.io.Serializable;

/**
 * класс содержания массива генотипов с целью возможности отправки по сети
 * @author dmitriz
 */
public class PackGenotype implements Serializable{
    //массив генотипов
    Genotype[] massiv;
    
    public PackGenotype(Genotype[] mass){
        massiv=mass;
    }
    //извлечение массива
    public Genotype[] get(){
        return massiv;
    }
}
