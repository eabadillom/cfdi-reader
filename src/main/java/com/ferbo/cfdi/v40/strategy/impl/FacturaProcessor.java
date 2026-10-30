package com.ferbo.cfdi.v40.strategy.impl;

import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.ferbo.cfdi.v40.model.TipoCFDI;
import com.ferbo.cfdi.v40.strategy.CFDIProcessor;

import mx.gob.sat.cfdi.v40.Comprobante;

public class FacturaProcessor implements CFDIProcessor {
	private static final Logger log = LogManager.getLogger(FacturaProcessor.class);
	
	@Override
	public TipoCFDI getTipoSoportado() {
		return TipoCFDI.FACTURA;
	}

	@Override
	public void procesar(Comprobante comprobante) {
		log.info("Procesando conceptos e impuestos de una Factura Estándar...");
	}

	@Override
	public void procesar(Comprobante comprobante, Map<String, Object> parametrosJasper) {
		log.info("Ejecutando negocio de Factura...");
	}
}
