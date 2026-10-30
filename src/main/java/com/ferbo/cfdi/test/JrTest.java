package com.ferbo.cfdi.test;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import mx.gob.sat.cfdi.catalogos.CMetodoPago;
import mx.gob.sat.cfdi.catalogos.CMoneda;
import mx.gob.sat.cfdi.catalogos.CTipoDeComprobante;
import mx.gob.sat.cfdi.catalogos.CUsoCFDI;
import mx.gob.sat.cfdi.nomina12.Nomina;
import mx.gob.sat.cfdi.pagos20.Pagos;
import mx.gob.sat.cfdi.v40.Comprobante;
import mx.gob.sat.sitio_internet.cfd.catalogos.nomina.CTipoNomina;
// AJUSTA estos dos al paquete que defina tu cfdi.xjb (generados bajo mx.gob.sat.cfdi...)
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.view.JasperViewer;


/**
 * Utilidad estatica para probar reportes Jasper con datos de ejemplo.
 *
 * Uso desde IDE / Maven:
 *   JrTest.main(new String[] { "factura", "reports/factura/factura.jrxml" });
 *   JrTest.main(new String[] { "pagos",   "reports/pagos/pagos.jrxml" });
 *   JrTest.main(new String[] { "nomina",  "reports/nomina/nomina.jrxml" });
 *
 * Opcional 3er argumento: "pdf" para exportar a target/jrtest/<tipo>.pdf en lugar de abrir el visor.
 */
public final class JrTest {

    private static final String SUBREPORT_DIR = "reports/";
    private static final String OUT_DIR = "target/jrtest";

    private JrTest() {
    }

    public static void main(String[] args) throws Exception {
        String tipo = args.length > 0 ? args[0] : "factura";
        String reporte = args.length > 1 ? args[1] : "reports/factura/factura.jrxml";
        boolean pdf = args.length > 2 && "pdf".equalsIgnoreCase(args[2]);

        Map<String, Object> params;
        switch (tipo.toLowerCase()) {
            case "pagos":
                params = paramsPagos();
                break;
            case "nomina":
                params = paramsNomina();
                break;
            default:
                params = paramsFactura();
        }
        run(reporte, params, comprobanteDS(), tipo, pdf);
    }

    // ------------------------------------------------------------------
    // Ejecucion
    // ------------------------------------------------------------------

    public static void run(String reporteClasspath, Map<String, Object> params, JRDataSource principal,
            String nombre, boolean pdf) throws Exception {
        JasperReport report = cargar(reporteClasspath);
        // DataSource principal: 1 registro = el Comprobante. El resto llega por parametros.
        JasperPrint print = JasperFillManager.fillReport(report, params, principal);

        if (pdf) {
            new File(OUT_DIR).mkdirs();
            String destino = OUT_DIR + "/" + nombre + ".pdf";
            JasperExportManager.exportReportToPdfFile(print, destino);
            System.out.println("PDF generado: " + new File(destino).getAbsolutePath());
        } else {
            JasperViewer.viewReport(print, true);
        }
    }

    /** Acepta .jrxml (compila al vuelo) o .jasper (ya compilado), desde el classpath. */
    private static JasperReport cargar(String ruta) throws Exception {
        InputStream in = JrTest.class.getClassLoader().getResourceAsStream(ruta);
        if (in == null) {
            throw new IllegalArgumentException("No se encontro en el classpath: " + ruta);
        }
        try {
            if (ruta.endsWith(".jasper")) {
                return (JasperReport) net.sf.jasperreports.engine.util.JRLoader.loadObject(in);
            }
            return JasperCompileManager.compileReport(in);
        } finally {
            in.close();
        }
    }

    // ------------------------------------------------------------------
    // Parametros por tipo de comprobante
    // ------------------------------------------------------------------

    public static Map<String, Object> paramsFactura() {
        Map<String, Object> p = new HashMap<String, Object>();
        p.put("SUBREPORT_DIR", SUBREPORT_DIR);
        p.put("RFC_EMISOR", "EKU9003173C9");
        p.put("RFC_RECEPTOR", "XAXX010101000");
        p.put("TOTAL", new BigDecimal("1160.00"));
        p.put("DS_CONCEPTOS", conceptos());
        return p;
    }

    public static Map<String, Object> paramsPagos() {
        Map<String, Object> p = paramsFactura();
        p.put("TOTAL", BigDecimal.ZERO);
        p.put("DS_PAGOS", pagos());
        return p;
    }

    public static Map<String, Object> paramsNomina() {
        Map<String, Object> p = paramsFactura();
        p.put("RFC_RECEPTOR", "XEXX010101000");
        p.put("TOTAL", new BigDecimal("9500.00"));

        p.put("DS_NOMINA_CORE", nominaCore());
        p.put("DS_PERCEPCIONES", percepciones());
        p.put("DS_DEDUCCIONES", deducciones());
        return p;
    }

    // ------------------------------------------------------------------
    // Datos de ejemplo (usa los beans reales de cfdi40-model)
    // ------------------------------------------------------------------

    /**
     * DataSource PRINCIPAL del reporte: una lista con un solo Comprobante.
     * En Jasper los fields se declaran con rutas anidadas, p. ej.:
     * serie, folio, fecha, total, emisor.rfc, emisor.nombre, receptor.rfc, receptor.usoCFDI
     */
    public static java.util.Collection<Comprobante> comprobanteLista() {
        // Para el Data Adapter "Collection of JavaBeans" de Jaspersoft Studio
        // (exige un metodo estatico que devuelva una Collection o un arreglo de beans).
        return java.util.Collections.singletonList(comprobante());
    }

    public static List<Pagos.Pago> pagosLista() {
        return PagosTest.generarPagos();
    }

    public static JRDataSource comprobanteDS() {
        return new JRBeanCollectionDataSource(java.util.Collections.singletonList(comprobante()));
    }

    /** Comprobante de ejemplo (nuevo en cada llamada). */
    public static Comprobante comprobante() {
        Comprobante c = new Comprobante();
        c.setVersion("4.0");
        c.setSerie("A");
        c.setFolio("1001");
        c.setFecha(ahora());
        c.setFormaPago("03");
        c.setMetodoPago(CMetodoPago.PUE);
        c.setMoneda(CMoneda.MXN);
        c.setTipoDeComprobante(CTipoDeComprobante.I);
        c.setExportacion("01");
        c.setLugarExpedicion("06600");

        Comprobante.Emisor em = new Comprobante.Emisor();
        em.setRfc("EKU9003173C9");
        em.setNombre("ESCUELA KEMPER URGATE");
        em.setRegimenFiscal("601");
        c.setEmisor(em);

        Comprobante.Receptor re = new Comprobante.Receptor();
        re.setRfc("XAXX010101000");
        re.setNombre("PUBLICO EN GENERAL");
        re.setDomicilioFiscalReceptor("06600");
        re.setRegimenFiscalReceptor("616");
        re.setUsoCFDI(CUsoCFDI.G_03);
        c.setReceptor(re);

        Comprobante.Conceptos cs = new Comprobante.Conceptos();
        cs.getConcepto().addAll(listaConceptos());
        c.setConceptos(cs);

        BigDecimal subTotal = BigDecimal.ZERO;
        for (Comprobante.Conceptos.Concepto k : cs.getConcepto()) {
            subTotal = subTotal.add(k.getImporte());
        }
        c.setSubTotal(subTotal);
        c.setTotal(subTotal.multiply(new BigDecimal("1.16")).setScale(2, java.math.RoundingMode.HALF_UP));
        return c;
    }

    public static JRDataSource conceptos() {
        return new JRBeanCollectionDataSource(listaConceptos());
    }

    private static List<Comprobante.Conceptos.Concepto> listaConceptos() {
        List<Comprobante.Conceptos.Concepto> lista = new ArrayList<Comprobante.Conceptos.Concepto>();
        for (int i = 1; i <= 15; i++) { // 15 filas para probar salto de pagina
            Comprobante.Conceptos.Concepto c = new Comprobante.Conceptos.Concepto();
            c.setClaveProdServ("01010101");
            c.setNoIdentificacion("SKU-" + i);
            c.setCantidad(new BigDecimal(i));
            c.setClaveUnidad("H87");
            c.setUnidad("Pieza");
            c.setDescripcion("Concepto de prueba numero " + i + " con una descripcion lo bastante larga "
                    + "para verificar el ajuste de texto en el reporte");
            c.setValorUnitario(new BigDecimal("100.00"));
            c.setImporte(new BigDecimal("100.00").multiply(new BigDecimal(i)));
            c.setObjetoImp("02");
            lista.add(c);
        }
        return lista;
    }

    public static JRDataSource pagos() {
        List<Pagos.Pago> lista = new ArrayList<Pagos.Pago>();
        for (int i = 1; i <= 2; i++) {
            Pagos.Pago pago = new Pagos.Pago();
            pago.setFechaPago(ahora());
            pago.setFormaDePagoP("03");
            pago.setMonedaP(CMoneda.MXN);
            pago.setMonto(new BigDecimal("1160.00"));
            pago.setNumOperacion("OP-000" + i);

            Pagos.Pago.DoctoRelacionado d = new Pagos.Pago.DoctoRelacionado();
            d.setIdDocumento("11111111-2222-3333-4444-55555555555" + i);
            d.setSerie("A");
            d.setFolio("10" + i);
            d.setMonedaDR(CMoneda.MXN);
            d.setNumParcialidad(BigInteger.ONE);
            d.setImpSaldoAnt(new BigDecimal("2320.00"));
            d.setImpPagado(new BigDecimal("1160.00"));
            d.setImpSaldoInsoluto(new BigDecimal("1160.00"));
            pago.getDoctoRelacionado().add(d);

            lista.add(pago);
        }
        return new JRBeanCollectionDataSource(lista);
    }

    public static JRDataSource nominaCore() {
        return new JRBeanCollectionDataSource(java.util.Collections.singletonList(nomina()));
    }

    public static JRDataSource percepciones() {
        return new JRBeanCollectionDataSource(nomina().getPercepciones().getPercepcion());
    }

    public static JRDataSource deducciones() {
        return new JRBeanCollectionDataSource(nomina().getDeducciones().getDeduccion());
    }

    private static Nomina nomina() {
        Nomina n = new Nomina();
        n.setVersion("1.2");
        n.setTipoNomina(CTipoNomina.O);
        n.setFechaPago(ahora());
        n.setFechaInicialPago(ahora());
        n.setFechaFinalPago(ahora());
        n.setNumDiasPagados(new BigDecimal("15"));
        n.setTotalPercepciones(new BigDecimal("10000.00"));
        n.setTotalDeducciones(new BigDecimal("500.00"));

        Nomina.Percepciones per = new Nomina.Percepciones();
        per.getPercepcion().add(percepcion("001", "P001", "Sueldos, Salarios", "9000.00", "0.00"));
        per.getPercepcion().add(percepcion("019", "P002", "Horas extra", "500.00", "500.00"));
        n.setPercepciones(per);

        Nomina.Deducciones ded = new Nomina.Deducciones();
        ded.getDeduccion().add(deduccion("002", "D001", "ISR", "350.00"));
        ded.getDeduccion().add(deduccion("001", "D002", "Seguridad social", "150.00"));
        n.setDeducciones(ded);
        return n;
    }

    private static Nomina.Percepciones.Percepcion percepcion(String tipo, String clave, String concepto,
            String gravado, String exento) {
        Nomina.Percepciones.Percepcion x = new Nomina.Percepciones.Percepcion();
        x.setTipoPercepcion(tipo);
        x.setClave(clave);
        x.setConcepto(concepto);
        x.setImporteGravado(new BigDecimal(gravado));
        x.setImporteExento(new BigDecimal(exento));
        return x;
    }

    private static Nomina.Deducciones.Deduccion deduccion(String tipo, String clave, String concepto,
            String importe) {
        Nomina.Deducciones.Deduccion x = new Nomina.Deducciones.Deduccion();
        x.setTipoDeduccion(tipo);
        x.setClave(clave);
        x.setConcepto(concepto);
        x.setImporte(new BigDecimal(importe));
        return x;
    }

    private static XMLGregorianCalendar ahora() {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(new GregorianCalendar());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}