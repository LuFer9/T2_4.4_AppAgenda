/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appagenda;

import entidades.Provincia;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

/**
 *
 * @author Luis
 */
public class Consultas {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        EntityManagerFactory emf=
        Persistence.createEntityManagerFactory("AppAgendaPU");
        EntityManager em=emf.createEntityManager();
        
        //obtenemos la lista de provincias de la base de datos        
        Query queryProvincias = em.createNamedQuery("Provincia.findAll");
        
        //Obtenemos la lista de provincia que nos devuelve resultList
        List<Provincia> listProvincias = queryProvincias.getResultList();
        
        //Muestra las provincias almacenadas en la base de datos
        for(int i=0;i<listProvincias.size();i++){
            Provincia provincia=listProvincias.get(i);
            System.out.println(provincia.getNombre());
        }
        
        //Para hacer consultas determinadas con una condicion
        /*@NamedQuery(name="Provincia.findByNombre",query="SELECT p FROM
        Provincia p WHERE p.nombre = :nombre")})
        */
        
        //obtener todos los objetos provincia cuyo nombre sea Cádiz mostrando ID y nombre
        Query queryProvinciaCadiz = em.createNamedQuery("Provincia.findByNombre");
        
        queryProvinciaCadiz.setParameter("nombre", "Cádiz");
        List<Provincia> listProvinciasCadiz =queryProvinciaCadiz.getResultList();
            for(Provincia provinciaCadiz:listProvinciasCadiz){
            System.out.print(provinciaCadiz.getId()+":");
            System.out.println(provinciaCadiz.getNombre());
        }
            
        
        //Para obtener el objeto provincia que sea de ID 2
        //<T> T find(Class<T> entityClass, Object primaryKey)
        Provincia provinciaId2 = em.find(Provincia.class, 2);
        
        if (provinciaId2 != null){
            System.out.print(provinciaId2.getId() + ":");
            System.out.println(provinciaId2.getNombre());
        } else {
            System.out.println("No hay ninguna provincia con ID=2");
        }

        //asigna el Codigo CA no ID, a quellos objetos que tengan el nombre de cadiz
        //Query queryProvinciaCadiz = em.createNamedQuery("Provincia.findByNombre");
        queryProvinciaCadiz.setParameter("nombre", "Cádiz");
        //List<Provincia> listProvinciasCadiz =queryProvinciaCadiz.getResultList();
        em.getTransaction().begin();
        for(Provincia provinciaCadiz : listProvinciasCadiz){
            provinciaCadiz.setCodigo("CA");
            em.merge(provinciaCadiz);
            }
            em.getTransaction().commit();
            
        //Para eliminar un objeto cuyo ID sea 15
        //void remove(Object entity)
        Provincia provinciaId15 = em.find(Provincia.class, 15);
        em.getTransaction().begin();
        if (provinciaId15 != null){
            em.remove(provinciaId15);
        }else{
            System.out.println("No hay ninguna provincia con ID=15");
        }
            em.getTransaction().commit();
        }
    
    
    
    
}
