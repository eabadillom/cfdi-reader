package com.ferbo.cfdi.test;

import java.util.ArrayList;
import java.util.List;

import mx.gob.sat.cfdi.v40.Comprobante;

public class EmisorTest {

     /**
      * Genera una lista con un único Emisor de prueba válido para CFDI 4.0.
      * @return List<Comprobante.Emisor>
      */
     public static List<Comprobante.Emisor> generarEmisor() {
          List<Comprobante.Emisor> listaEmisor = new ArrayList<>();
          
          Comprobante.Emisor emisor = new Comprobante.Emisor();
          // RFC de prueba oficial del SAT para personas morales
          emisor.setRfc("EKU9003173C9"); 
          // Nombre en MAYÚSCULAS y sin régimen societario (sin "S.A. de C.V.")
          emisor.setNombre("ESCUELA KEMPER URGATE"); 
          // Régimen Fiscal (601 - General de Ley Personas Morales)
          emisor.setRegimenFiscal("601"); 
          
          listaEmisor.add(emisor);
          return listaEmisor;
     }
}
