package com.ferbo.cfdi.test;

import java.util.ArrayList;
import java.util.List;

import mx.gob.sat.cfdi.catalogos.CUsoCFDI;
import mx.gob.sat.cfdi.v40.Comprobante;

public class ReceptorTest {

     /**
      * Genera una lista con un único Receptor de prueba válido para CFDI 4.0.
      * @return List<Comprobante.Receptor>
      */
     public static List<Comprobante.Receptor> generarReceptor() {
          List<Comprobante.Receptor> listaReceptor = new ArrayList<>();
          
          Comprobante.Receptor receptor = new Comprobante.Receptor();
          // RFC de prueba oficial del SAT
          receptor.setRfc("URE180429I64"); 
          // Nombre en MAYÚSCULAS según Constancia de Situación Fiscal
          receptor.setNombre("UNIVERSIDAD REVOLUCION"); 
          // Código Postal requerido para el nodo de domicilio del receptor
          receptor.setDomicilioFiscalReceptor("64000"); 
          // Régimen Fiscal del Receptor (603 - Personas Morales con Fines no Lucrativos)
          receptor.setRegimenFiscalReceptor("603"); 
          // Uso del CFDI (G03 - Gastos en general)
          receptor.setUsoCFDI(CUsoCFDI.G_03);
          
          listaReceptor.add(receptor);
          return listaReceptor;
     }
}

