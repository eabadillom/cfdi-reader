package com.ferbo.cfdi.readers;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import mx.gob.sat.cfdi.pagos20.Pagos;
import mx.gob.sat.cfdi.pagos20.Pagos.Pago;
import mx.gob.sat.cfdi.pagos20.Pagos.Totales;
import mx.gob.sat.cfdi.v40.Comprobante;


@Deprecated
public class ComplementoPagoReader implements CFDIReader {
    private static Logger log = LogManager.getLogger(ComplementoPagoReader.class);

    @Override
    public void read(String rutaXml) {
        try {
            log.info("Leyendo complemento de pago del archivo: {}", rutaXml);
            try {
        	File xmlFile = new File(rutaXml);
            if (!xmlFile.exists()) {
                System.err.println("El archivo no existe: " + rutaXml);
                System.exit(2);
            }
        	
            JAXBContext context = JAXBContext.newInstance(
                Comprobante.class,
                Pagos.class);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            Comprobante comprobante = (Comprobante) unmarshaller.unmarshal(xmlFile);

            comprobante.getComplemento().getAny().forEach(c -> {
                log.info("Complemento: {}", c.getClass().getName());
            });
            
            Pagos pagos = (Pagos) comprobante.getComplemento().getAny().get(0);
            log.info("Número de pagos: {}", pagos.getPago().size());

            Totales totales = pagos.getTotales();
            log.info("Total traslados base IVA 16%: {}", totales.getTotalTrasladosBaseIVA16());
            log.info("Total traslados importe IVA 16%: {}", totales.getTotalTrasladosImpuestoIVA16());
            log.info("Total retenciones importe IVA: {}", totales.getTotalRetencionesIVA());
            log.info("Total retenciones importe ISR: {}", totales.getTotalRetencionesISR());
            log.info("Total retenciones importe IEPS: {}", totales.getTotalRetencionesIEPS());
            log.info("Total de pagos: {}", totales.getMontoTotalPagos());

            pagos.getPago().forEach(p -> {
                log.info("PAGO - Fecha de pago: {}", p.getFechaPago());
                log.info("PAGO - Forma de pago: {}", p.getFormaDePagoP());
                log.info("PAGO - Moneda: {}", p.getMonedaP());
                log.info("PAGO - Monto: {}", p.getMonto());
                log.info("PAGO - Num. Operación: {}", p.getNumOperacion());
                log.info("PAGO - Rfc Emisor Cta Ord: {}", p.getRfcEmisorCtaOrd());
                log.info("PAGO - Cta. Ordenante: {}", p.getCtaOrdenante());
                log.info("PAGO - Rfc Emisor Cta Ben: {}", p.getRfcEmisorCtaBen());
                log.info("PAGO - Cta. Beneficiario: {}", p.getCtaBeneficiario());

                Pago.DoctoRelacionado doctoRelacionado = p.getDoctoRelacionado().get(0);
                log.info("PAGO - Docto Relacionado - Id Documento: {}", doctoRelacionado.getIdDocumento());
                log.info("PAGO - Docto Relacionado - Serie: {}", doctoRelacionado.getSerie());
                log.info("PAGO - Docto Relacionado - Folio: {}", doctoRelacionado.getFolio());
                log.info("PAGO - Docto Relacionado - Moneda: {}", doctoRelacionado.getMonedaDR());
                log.info("PAGO - Docto Relacionado - Num. Parcialidad: {}", doctoRelacionado.getNumParcialidad());
                log.info("PAGO - Docto Relacionado - Imp. Saldo Ant: {}", doctoRelacionado.getImpSaldoAnt());
                log.info("PAGO - Docto Relacionado - Imp. Pagado: {}", doctoRelacionado.getImpPagado());
                log.info("PAGO - Docto Relacionado - Imp. Saldo Insoluto: {}", doctoRelacionado.getImpSaldoInsoluto());
                log.info("PAGO - Docto Relacionado - Objeto de Impuesto: {}", doctoRelacionado.getObjetoImpDR());
            });

        } catch (Exception ex) {
            log.error("Problema para leer el CFDI...", ex);
        }
        } catch (Exception ex) {
            log.error("Problema para leer el complemento de pago...", ex);
        }
    }
}
