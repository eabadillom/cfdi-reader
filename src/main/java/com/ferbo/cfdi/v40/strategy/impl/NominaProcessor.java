package com.ferbo.cfdi.v40.strategy.impl;

import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ferbo.cfdi.v40.model.TipoCFDI;
import com.ferbo.cfdi.v40.strategy.CFDIProcessor;

import mx.gob.sat.cfdi.nomina12.Nomina;
import mx.gob.sat.cfdi.v40.Comprobante;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class NominaProcessor implements CFDIProcessor {
	
	private static final Logger log = LogManager.getLogger(NominaProcessor.class);
	
	@Override
	public TipoCFDI getTipoSoportado() {
		return TipoCFDI.NOMINA;
	}

	@Override
	public void procesar(Comprobante comprobante) {
		log.info("Procesando recibo de nómina (Sueldos, Retenciones)...");
	}

	@Override
	public void procesar(Comprobante comprobante, Map<String, Object> parametrosJasper) {
		log.info("Ejecutando negocio de Nómina y extrayendo percepciones/deducciones...");

        if (comprobante.getComplemento() != null) {
            for (Object comp : comprobante.getComplemento().getAny()) {
                if (comp instanceof Nomina) {
                    Nomina nomina = (Nomina) comp;
                    
                    // Pasamos el objeto Nómina completo en una lista para el subreporte principal de nómina
                    parametrosJasper.put("DS_NOMINA_CORE", new JRBeanCollectionDataSource(Collections.singletonList(nomina)));
                    
                    // Si el diseño de tu Jasper requiere separar Percepciones de Deducciones en tablas independientes:
                    if (nomina.getPercepciones() != null) {
                        parametrosJasper.put("DS_PERCEPCIONES", new JRBeanCollectionDataSource(nomina.getPercepciones().getPercepcion()));
                    }
                    if (nomina.getDeducciones() != null) {
                        parametrosJasper.put("DS_DEDUCCIONES", new JRBeanCollectionDataSource(nomina.getDeducciones().getDeduccion()));
                    }
                    break;
                }
            }
        }
        parametrosJasper.put("SUBREPORT_DIR", "reports/nomina/v12/");
	}
}
