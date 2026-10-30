package com.ferbo.cfdi.test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import mx.gob.sat.cfdi.catalogos.CMoneda;
import mx.gob.sat.cfdi.catalogos.CTipoFactor;
import mx.gob.sat.cfdi.pagos20.Pagos;

public class PagosTest {
    public static List<Pagos.Pago> generarPagos() {
        List<Pagos.Pago> lista = new ArrayList<Pagos.Pago>();

        // =========================================================================
        // PAGO 1: Transferencia (03) - Parcialidad 1 de Factura de $2,320.00
        // =========================================================================
        Pagos.Pago p1 = new Pagos.Pago();
        p1.setFechaPago(ahora());
        p1.setFormaDePagoP("03");
        p1.setMonedaP(CMoneda.MXN);
        p1.setMonto(new BigDecimal("1160.00"));
        p1.setNumOperacion("OP-0001");

        Pagos.Pago.DoctoRelacionado d1 = new Pagos.Pago.DoctoRelacionado();
        d1.setIdDocumento("A1B2C3D4-E5F6-7A8B-9C0D-1E2F3A4B5C6D");
        d1.setSerie("A");
        d1.setFolio("101");
        d1.setMonedaDR(CMoneda.MXN);
        d1.setNumParcialidad(BigInteger.valueOf(1));
        d1.setImpSaldoAnt(new BigDecimal("2320.00"));
        d1.setImpPagado(new BigDecimal("1160.00"));
        d1.setImpSaldoInsoluto(new BigDecimal("1160.00"));
        d1.setObjetoImpDR("02"); // Sí objeto de impuesto

        agregarIva16DR(d1);
        p1.getDoctoRelacionado().add(d1);
        calcularImpuestosPago(p1);
        lista.add(p1);

        // =========================================================================
        // PAGO 2: Cheque (02) - Pago Único Completo de $5,800.00
        // =========================================================================
        Pagos.Pago p2 = new Pagos.Pago();
        p2.setFechaPago(ahora());
        p2.setFormaDePagoP("02");
        p2.setMonedaP(CMoneda.MXN);
        p2.setMonto(new BigDecimal("5800.00"));
        p2.setNumOperacion("CHQ-99212");

        Pagos.Pago.DoctoRelacionado d2 = new Pagos.Pago.DoctoRelacionado();
        d2.setIdDocumento("9F8E7D6C-5B4A-3F2E-1D0C-9B8A7F6E5D4C");
        d2.setSerie("F");
        d2.setFolio("2045");
        d2.setMonedaDR(CMoneda.MXN);
        d2.setNumParcialidad(BigInteger.valueOf(1));
        d2.setImpSaldoAnt(new BigDecimal("5800.00"));
        d2.setImpPagado(new BigDecimal("5800.00"));
        d2.setImpSaldoInsoluto(new BigDecimal("0.00"));
        d2.setObjetoImpDR("02");

        agregarIva16DR(d2);
        p2.getDoctoRelacionado().add(d2);
        calcularImpuestosPago(p2);
        lista.add(p2);

        // =========================================================================
        // PAGO 3: Un Único Pago ($3,000.00) que ampara DOS Facturas Diferentes
        // =========================================================================
        Pagos.Pago p3 = new Pagos.Pago();
        p3.setFechaPago(ahora());
        p3.setFormaDePagoP("03");
        p3.setMonedaP(CMoneda.MXN);
        p3.setMonto(new BigDecimal("3000.00"));
        p3.setNumOperacion("OP-8821");

        // Factura 1 vinculada al pago 3
        Pagos.Pago.DoctoRelacionado d3_1 = new Pagos.Pago.DoctoRelacionado();
        d3_1.setIdDocumento("D4E5F6A7-B8C9-0D1E-2F3A-4B5C6D7E8F9A");
        d3_1.setSerie("A");
        d3_1.setFolio("701");
        d3_1.setMonedaDR(CMoneda.MXN);
        d3_1.setNumParcialidad(BigInteger.valueOf(1));
        d3_1.setImpSaldoAnt(new BigDecimal("2000.00"));
        d3_1.setImpPagado(new BigDecimal("2000.00"));
        d3_1.setImpSaldoInsoluto(new BigDecimal("0.00"));
        d3_1.setObjetoImpDR("02");
        agregarIva16DR(d3_1);
        p3.getDoctoRelacionado().add(d3_1);

        // Factura 2 vinculada al pago 3
        Pagos.Pago.DoctoRelacionado d3_2 = new Pagos.Pago.DoctoRelacionado();
        d3_2.setIdDocumento("E5F6A7B8-C9D0-1E2F-3A4B-5C6D7E8F9A0B");
        d3_2.setSerie("A");
        d3_2.setFolio("702");
        d3_2.setMonedaDR(CMoneda.MXN);
        d3_2.setNumParcialidad(BigInteger.valueOf(1));
        d3_2.setImpSaldoAnt(new BigDecimal("1500.00"));
        d3_2.setImpPagado(new BigDecimal("1000.00"));
        d3_2.setImpSaldoInsoluto(new BigDecimal("500.00"));
        d3_2.setObjetoImpDR("02");
        agregarIva16DR(d3_2);
        p3.getDoctoRelacionado().add(d3_2);

        calcularImpuestosPago(p3);
        lista.add(p3);

        return lista;
    }

    private static void agregarIva16DR(Pagos.Pago.DoctoRelacionado dr) {
        BigDecimal impuestoFactor = new BigDecimal("0.16");
        BigDecimal divisor = new BigDecimal("1.16");
        
        BigDecimal base = dr.getImpPagado().divide(divisor, 6, RoundingMode.HALF_UP);
        BigDecimal importe = base.multiply(impuestoFactor).setScale(6, RoundingMode.HALF_UP);

        Pagos.Pago.DoctoRelacionado.ImpuestosDR impuestosDR = new Pagos.Pago.DoctoRelacionado.ImpuestosDR();
        Pagos.Pago.DoctoRelacionado.ImpuestosDR.TrasladosDR trasladosDR = new Pagos.Pago.DoctoRelacionado.ImpuestosDR.TrasladosDR();
        Pagos.Pago.DoctoRelacionado.ImpuestosDR.TrasladosDR.TrasladoDR t = new Pagos.Pago.DoctoRelacionado.ImpuestosDR.TrasladosDR.TrasladoDR();
        
        t.setBaseDR(base.setScale(2, RoundingMode.HALF_UP));
        t.setImpuestoDR("002");
        t.setTipoFactorDR(CTipoFactor.TASA);
        t.setTasaOCuotaDR(new BigDecimal("0.160000"));
        t.setImporteDR(importe.setScale(2, RoundingMode.HALF_UP));
        
        trasladosDR.getTrasladoDR().add(t);
        impuestosDR.setTrasladosDR(trasladosDR);
        dr.setImpuestosDR(impuestosDR);
    }

    private static void calcularImpuestosPago(Pagos.Pago pago) {
        BigDecimal sumaBase = BigDecimal.ZERO;
        BigDecimal sumaImporte = BigDecimal.ZERO;

        for (Pagos.Pago.DoctoRelacionado dr : pago.getDoctoRelacionado()) {
            if (dr.getImpuestosDR() != null && dr.getImpuestosDR().getTrasladosDR() != null) {
                for (Pagos.Pago.DoctoRelacionado.ImpuestosDR.TrasladosDR.TrasladoDR tDR : dr.getImpuestosDR().getTrasladosDR().getTrasladoDR()) {
                    sumaBase = sumaBase.add(tDR.getBaseDR());
                    sumaImporte = sumaImporte.add(tDR.getImporteDR());
                }
            }
        }

        Pagos.Pago.ImpuestosP impuestosP = new Pagos.Pago.ImpuestosP();
        Pagos.Pago.ImpuestosP.TrasladosP trasladosP = new Pagos.Pago.ImpuestosP.TrasladosP();
        Pagos.Pago.ImpuestosP.TrasladosP.TrasladoP tP = new Pagos.Pago.ImpuestosP.TrasladosP.TrasladoP();

        tP.setBaseP(sumaBase.setScale(2, RoundingMode.HALF_UP));
        tP.setImpuestoP("002");
        tP.setTipoFactorP(CTipoFactor.TASA);
        tP.setTasaOCuotaP(new BigDecimal("0.160000"));
        tP.setImporteP(sumaImporte.setScale(2, RoundingMode.HALF_UP));

        trasladosP.getTrasladoP().add(tP);
        impuestosP.setTrasladosP(trasladosP);
        pago.setImpuestosP(impuestosP);
    }

    private static XMLGregorianCalendar ahora() {
        try {
            GregorianCalendar gCal = new GregorianCalendar();
            // Inicialización explícita estándar de Java SE 8
            DatatypeFactory factory = DatatypeFactory.newInstance();
            return factory.newXMLGregorianCalendar(gCal);
        } catch (DatatypeConfigurationException e) {
            throw new RuntimeException("Error en la configuración del proveedor XML de Java", e);
        }

    }

}
