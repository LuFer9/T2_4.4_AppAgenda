/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package appagenda;

import entidades.Provincia;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

/**
 *
 * @author Luis
 */
public class AppAgenda {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        EntityManagerFactory emf=
        Persistence.createEntityManagerFactory("AppAgendaPU");
        EntityManager em=emf.createEntityManager();
        
        /*
        //iniciamos una transaccion en la que podemos realizr cualquier numero de opreaciones de modificacion
        em.getTransaction().begin();
        
        
        //Una vez realaizadas las operaciones se debe confirmar su volcado
        em.getTransaction().commit();
        
        //Si no queremos confirmar los datos antes de hacer un Commit NO despues
        em.getTransaction().rollback();
        */
        
   
        //creamos un objeto provincia para introducrilo en la base de datos
        Provincia provinciaSevilla = new Provincia();
        provinciaSevilla.setNombre("Cádiz");
        
        em.getTransaction().begin();
        em.persist(provinciaSevilla); // Iniciamos la transaccion sobre este objeto en especifico
        em.getTransaction().commit();
        
        
        
        em.close();
        emf.close();
      
        
        try{
            DriverManager.getConnection("jdbc:derby:BDAgenda;shutdown=true");
        } catch (SQLException ex){
        }
        

    }
    
}
