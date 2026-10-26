package com.ferbo.cfdi.v40.service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

import com.ferbo.cfdi.v40.model.CFDIPrintWrapper;
import com.ferbo.cfdi.v40.model.TipoCFDI;
import com.ferbo.cfdi.v40.strategy.CFDIProcessor;
import com.ferbo.cfdi.v40.strategy.impl.FacturaProcessor;
import com.ferbo.cfdi.v40.strategy.impl.NominaProcessor;
import com.ferbo.cfdi.v40.strategy.impl.PagoProcessor;

import mx.gob.sat.cfdi.nomina12.Nomina;
import mx.gob.sat.cfdi.pagos20.Pagos;
import mx.gob.sat.cfdi.v40.Comprobante;
import mx.gob.sat.cfdi.v40.Comprobante.Conceptos.Concepto;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class CFDIReaderService {

    private static final Map<TipoCFDI, CFDIProcessor> procesadores = new HashMap<>();

    static {
        procesadores.put(TipoCFDI.FACTURA, new FacturaProcessor());
        procesadores.put(TipoCFDI.COMPLEMENTO_PAGO, new PagoProcessor());
        procesadores.put(TipoCFDI.NOMINA, new NominaProcessor());
    }

    /**
     * Lee el XML desde un Stream en memoria, ejecuta su lógica de negocio 
     * específica y retorna el Comprobante listo para JasperReports.
     */
    public Comprobante ejecutarCarga(InputStream xmlStream) throws Exception {
        if (xmlStream == null)
        	throw new IllegalArgumentException("El InputStream del XML no puede ser nulo.");

        // 1. JAXB procesa el InputStream directamente en memoria
        //TODO Si se agregan más complementos del SAT, esta línea deberá agregar cada una de las clases que representan a cada complemento de pago.
        JAXBContext context = JAXBContext.newInstance(Comprobante.class, Pagos.class, Nomina.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Comprobante comprobante = (Comprobante) unmarshaller.unmarshal(xmlStream);

        // 2. Detectar el tipo de CFDI
        TipoCFDI tipoDetectado = TipoCFDI.detectar(comprobante);

        // 3. Buscar y ejecutar su estrategia de negocio
        CFDIProcessor procesador = procesadores.get(tipoDetectado);
        if (procesador == null) {
            throw new UnsupportedOperationException("No se ha registrado un procesador para: " + tipoDetectado);
        }

        // Se ejecuta la lógica (p.ej. guardar en BD, validar estatus, etc.)
        procesador.procesar(comprobante);

        // 4. Retornamos el objeto limpio y estructurado
        return comprobante;
    }
    
    public CFDIPrintWrapper ejecutarCargaJR(InputStream xmlStream) throws Exception {
        // 1. Parseo inicial en memoria
        JAXBContext context = JAXBContext.newInstance(Comprobante.class);
        Unmarshaller unmarshaller = context.createUnmarshaller();
        Comprobante comprobante = (Comprobante) unmarshaller.unmarshal(xmlStream);

        // 2. Inicializar el mapa de parámetros de Jasper
        Map<String, Object> parametrosJasper = new HashMap<>();

        // 3. Extraer e inyectar lo común de CUALQUIER CFDI 4.0
        if (comprobante.getConceptos() != null) {
            List<Concepto> listaConceptos = comprobante.getConceptos().getConcepto();
            parametrosJasper.put("DS_CONCEPTOS", new JRBeanCollectionDataSource(listaConceptos));
        }
        
        // Parámetros informativos directos si no quieres navegar el objeto en Jasper
        parametrosJasper.put("RFC_EMISOR", comprobante.getEmisor().getRfc());
        parametrosJasper.put("RFC_RECEPTOR", comprobante.getReceptor().getRfc());
        parametrosJasper.put("TOTAL", comprobante.getTotal());

        // 4. Determinar tipo y delegar extracción del complemento
        TipoCFDI tipoDetectado = TipoCFDI.detectar(comprobante);
        CFDIProcessor procesador = procesadores.get(tipoDetectado);
        
        if (procesador == null) {
            throw new UnsupportedOperationException("Sin procesador para: " + tipoDetectado);
        }

        // El procesador añade al mapa "DS_PAGOS", "DS_NOMINA", etc.
        procesador.procesar(comprobante, parametrosJasper);

        // 5. Retornar el paquete completo listo para imprimir
        return new CFDIPrintWrapper(comprobante, parametrosJasper);
    }
}
