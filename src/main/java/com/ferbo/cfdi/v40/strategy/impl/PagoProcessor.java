package com.ferbo.cfdi.v40.strategy.impl;

import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ferbo.cfdi.v40.model.TipoCFDI;
import com.ferbo.cfdi.v40.strategy.CFDIProcessor;

import mx.gob.sat.cfdi.pagos20.Pagos;
import mx.gob.sat.cfdi.pagos20.Pagos.Pago;
import mx.gob.sat.cfdi.pagos20.Pagos.Pago.DoctoRelacionado;
import mx.gob.sat.cfdi.pagos20.Pagos.Pago.DoctoRelacionado.ImpuestosDR;
import mx.gob.sat.cfdi.pagos20.Pagos.Pago.DoctoRelacionado.ImpuestosDR.RetencionesDR.RetencionDR;
import mx.gob.sat.cfdi.pagos20.Pagos.Pago.DoctoRelacionado.ImpuestosDR.TrasladosDR.TrasladoDR;
import mx.gob.sat.cfdi.v40.Comprobante;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class PagoProcessor implements CFDIProcessor {
	
	private static final Logger log = LogManager.getLogger(PagoProcessor.class);
	
	@Override
    public TipoCFDI getTipoSoportado() {
        return TipoCFDI.COMPLEMENTO_PAGO;
    }
	
    @Override
    public void procesar(Comprobante comprobante) {
    	log.info("Iniciando procesamiento de nodos de la sección Complemento -> Pagos 2.0...");

        if (comprobante.getComplemento() == null || comprobante.getComplemento().getAny() == null) {
            log.warn("El CFDI está marcado como Pago pero no contiene ningún nodo de Complemento.");
            return;
        }
        
        comprobante.getComplemento().getAny().forEach(c -> {
            log.info("Complemento: {}", c.getClass().getName());
        });
        
        for (Object complementoObj : comprobante.getComplemento().getAny()) {
            
            if (complementoObj instanceof Pagos) {
            	Pagos complementoPagos = (Pagos) complementoObj;
            	this.procesar(complementoPagos);
            	break;
            }
        }
    }
    
    private void procesar(Pagos complementoPagos) {
    	
        log.info("Versión del Complemento de Pago detectada: " + complementoPagos.getVersion());

        int indicePago = 1;
        for (Pago pago : complementoPagos.getPago()) {
            log.info(String.format("--- Detalle del Pago #%d ---", indicePago++));
            log.info("Fecha de Pago: " + pago.getFechaPago());
            log.info("Forma de Pago: " + pago.getFormaDePagoP());
            log.info("Moneda: " + pago.getMonedaP());
            log.info("Monto Total Recibido: " + pago.getMonto());

            // Navegación a Documentos Relacionados
            if (pago.getDoctoRelacionado() != null && !pago.getDoctoRelacionado().isEmpty()) {
                for (DoctoRelacionado docRel : pago.getDoctoRelacionado()) {
                    log.info(String.format("   * UUID Relacionado: %s (Parcialidad: %s)", 
                            docRel.getIdDocumento(), docRel.getNumParcialidad()));
                    log.info(String.format("     Imp. Pagado: %s", docRel.getImpPagado()));

                    // --- EXTRACCIÓN DE IMPUESTOS A NIVEL DOCUMENTO RELACIONADO (ImpuestosDR) ---
                    ImpuestosDR impuestosDR = docRel.getImpuestosDR();
                    if (impuestosDR != null) {
                        
                        // 1. Procesar Traslados (ej. IVA 16%, IVA 8%, IEPS)
                        if (impuestosDR.getTrasladosDR() != null && impuestosDR.getTrasladosDR().getTrasladoDR() != null) {
                            for (TrasladoDR traslado : impuestosDR.getTrasladosDR().getTrasladoDR()) {
                                log.info(String.format("     [Traslado DR] Base: %s | Impuesto: %s | TipoFactor: %s | TasaOCuota: %s | Importe: %s",
                                        traslado.getBaseDR(),
                                        traslado.getImpuestoDR(),
                                        traslado.getTipoFactorDR(),
                                        traslado.getTasaOCuotaDR(),
                                        traslado.getImporteDR()));
                            }
                        }

                        // 2. Procesar Retenciones (ej. Retención de IVA, Retención de ISR)
                        if (impuestosDR.getRetencionesDR() != null && impuestosDR.getRetencionesDR().getRetencionDR() != null) {
                            for (RetencionDR retencion : impuestosDR.getRetencionesDR().getRetencionDR()) {
                                log.info(String.format("     [Retención DR] Base: %s | Impuesto: %s | TipoFactor: %s | TasaOCuota: %s | Importe: %s",
                                        retencion.getBaseDR(),
                                        retencion.getImpuestoDR(),
                                        retencion.getTipoFactorDR(),
                                        retencion.getTasaOCuotaDR(),
                                        retencion.getImporteDR()));
                            }
                        }
                        
                    } else {
                        log.info("     El documento relacionado no desglosa impuestos (ej. Operación Exenta o ObjetoImp = 01).");
                    }
                }
            }
        }
    }
    
    @Override
    public void procesar(Comprobante comprobante, Map<String, Object> parametrosJasper) {
        log.info("Ejecutando negocio de Pagos y extrayendo nodos hijos...");

        if (comprobante.getComplemento() != null) {
            for (Object comp : comprobante.getComplemento().getAny()) {
                // JAXB extrae el elemento mapeado (ej. la clase Pagos generada por xjc)
                if (comp instanceof Pagos) { 
                    Pagos pagos = (Pagos) comp;
                    
                    // Extraemos la lista de pagos individuales de la estructura del SAT
                    List<Pago> listaPagos = pagos.getPago();
                    
                    // Lo envolvemos en el Datasource independiente para el subreporte de pagos
                    parametrosJasper.put("DS_PAGOS", new JRBeanCollectionDataSource(listaPagos));
                    break;
                }
            }
        }
        parametrosJasper.put("SUBREPORT_DIR", "reports/pagos/v20/");
    }
}
