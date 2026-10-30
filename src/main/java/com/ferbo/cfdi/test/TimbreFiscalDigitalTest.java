package com.ferbo.cfdi.test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import mx.gob.sat.cfdi.tfd11.TimbreFiscalDigital;

public class TimbreFiscalDigitalTest {

     /**
      * Genera una lista con un único Timbre Fiscal Digital de prueba válido (TFD v1.1).
      * @return List<TimbreFiscalDigital>
      */
     public static List<TimbreFiscalDigital> generarTimbre() {
          List<TimbreFiscalDigital> listaTimbre = new ArrayList<>();
          
          try {
               TimbreFiscalDigital tfd = new TimbreFiscalDigital();
               
               // Versión fija del complemento
               tfd.setVersion("1.1");
               
               // UUID (Folio Fiscal) aleatorio de prueba
               tfd.setUUID(UUID.randomUUID().toString().toUpperCase());
               
               // Fecha de timbrado en formato XML (YYYY-MM-DDTHH:mm:ss)
               XMLGregorianCalendar fechaTimbrado = DatatypeFactory.newInstance()
                         .newXMLGregorianCalendar("2026-10-29T12:00:00");
               tfd.setFechaTimbrado(fechaTimbrado);
               
               // RFC del PAC que timbra (Usa el RFC de prueba oficial del SAT para PACs)
               tfd.setRfcProvCertif("NNM980114922");
               
               // Sello ficticio del CFDI (base64 simulado)
               tfd.setSelloCFD("selloDigitalCfdiFicticioBase64==");
               
               // Número de certificado del PAC de prueba
               tfd.setNoCertificadoSAT("00001000000504204441");
               
               // Sello ficticio del SAT (base64 simulado)
               tfd.setSelloSAT("selloDigitalSatFicticioBase64==");
               
               listaTimbre.add(tfd);
               
          } catch (Exception e) {
               e.printStackTrace();
          }
          
          return listaTimbre;
     }
}

