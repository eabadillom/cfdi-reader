package com.ferbo.cfdi.readers;

import java.io.File;
import java.util.List;
import java.util.Optional;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mx.gob.sat.cfdi.nomina12.Nomina;
import mx.gob.sat.cfdi.nomina12.Nomina.Percepciones.Percepcion;
import mx.gob.sat.cfdi.v40.Comprobante;

@Deprecated
public class NominaReader implements CFDIReader {
    private static Logger log = LogManager.getLogger(NominaReader.class);

    @Override
    public void read(String rutaXml) {
        try {
        	File xmlFile = new File(rutaXml);
            if (!xmlFile.exists()) {
                System.err.println("El archivo no existe: " + rutaXml);
                System.exit(2);
            }
        	
            JAXBContext context = JAXBContext.newInstance(Comprobante.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Comprobante c = (Comprobante) unmarshaller.unmarshal(xmlFile);
            Nomina n = (Nomina) c.getComplemento().getAny().get(0);
            List<Percepcion> percepcion = n.getPercepciones().getPercepcion();
            
            Optional<Percepcion> septimoDia = percepcion.stream().filter(p ->  "003".equalsIgnoreCase(p.getClave()))
            		.findFirst();

            log.info("Receptor: {}",c.getReceptor().getNombre());
            log.info("Regimen: {}", c.getReceptor().getRegimenFiscalReceptor());
            log.info("Curp: {}", n.getReceptor().getCurp());
            log.info("Domicilio: {}", c.getReceptor().getDomicilioFiscalReceptor());
            log.info("RFC: {}", c.getReceptor().getRfc());
            log.info("Fecha Inicio rel. laboral: {}", n.getReceptor().getFechaInicioRelLaboral());
            log.info("NSS: {}", n.getReceptor().getNumSeguridadSocial());
            log.info("Dias pagados: {}", n.getNumDiasPagados());
            log.info("Salario diario: {}", septimoDia.get().getImporteGravado());
            log.info("Empresa: {}", c.getEmisor().getNombre());
            log.info("Puesto: {}", n.getReceptor().getPuesto());
            log.info("Area: {}", n.getReceptor().getDepartamento());
            log.info("Tipo contrato: {}", n.getReceptor().getTipoContrato());
            log.info("Tipo jornada: {}", n.getReceptor().getTipoJornada());
            log.info("Tipo régimen: {}", n.getReceptor().getTipoRegimen());
            log.info("Clave ent. federativa: {}", n.getReceptor().getClaveEntFed());
            log.info("Riesgo puesto: {}", n.getReceptor().getRiesgoPuesto());
            log.info("Periodicidad pago: {}", n.getReceptor().getPeriodicidadPago());
            log.info("Sindizalizado: {}", n.getReceptor().getSindicalizado());
            
        } catch (Exception ex) {
            log.error("Problema para leer el CFDI...", ex);
        }
    }
}
