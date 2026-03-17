package com.celsa.SqlExtractor.repository;

import com.celsa.SqlExtractor.entity.consulta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface ExtranctorRepository extends JpaRepository<consulta, String> {

    @Query(value = """
                     /* ==========================================================\s
                        1. DEFINICIÓN DE PARÁMETROS (Según tu imagen)
                        ========================================================== */
                     DECLARE @Espacio_tiempo INT = 3;
                     DECLARE @Amortiguador   INT = 5;
                     DECLARE @FechaInicio    DATETIME = '2025-11-01'; -- 1 de Noviembre, 2025
                     DECLARE @FechaFin       DATETIME = '2025-11-06'; -- 6 de Noviembre, 2025

                     /* ==========================================================\s
                        2. CONSULTA PRINCIPAL (CTEs)
                        ========================================================== */
                     WITH tabla_inicial AS (
                         SELECT
                             om.ORDNUM_10,
                             om.ORDER_10,
                             om.LINNUM_10,
                             om.DELNUM_10,
                             om.PRTNUM_10,
                             th.TNXDTE_15,
                             om.TYPE_10,
                             om.STATUS_10,
                             th.TNXCDE_15,
                             vm.CNTRY_08,
                             om.TRNDTE_10,
                             vm.COMNAM_08 AS proveedor,
                             om.CURPRM_10,
                             th.TNXQTY_15 AS CANTIDAD_INGRESADA,
                             om.curqty_10 AS CANTIDAD_PEDIDO
                         FROM MAXCELSASA.dbo.Order_Master AS om
                         INNER JOIN MAXCELSASA.dbo.Transaction_History AS th
                             ON om.ORDER_10 = th.ORDNUM_15
                         INNER JOIN MAXCELSASA.dbo.Vendor_Master AS vm
                             ON om.VENID_10 = vm.VENID_08
                         WHERE
                             om.STATUS_10 IN ('4', '5') AND
                             om.TYPE_10 IN ('PO', 'SO') AND
                             --th.TNXDTE_15 >= @FechaInicio AND
                             --th.TNXDTE_15 <= @FechaFin AND
                     		YEAR(th.TNXDTE_15)=YEAR(GETDATE()) AND
                     		MONTH(th.TNXDTE_15)=MONTH(GETDATE()) AND

                             th.TNXCDE_15 = 'R' AND
                             th.TNXQTY_15 > 0
                     ), ORIGEN AS (
                         SELECT *,
                             CASE
                                 WHEN CNTRY_08 = 'COLOMBIA' THEN 'NACIONAL'
                                 ELSE 'IMPORT'
                             END AS origen_proveedor
                         FROM tabla_inicial
                     ), TABLA_FECHA AS (
                         SELECT *,
                             CASE
                                 WHEN origen_proveedor = 'NACIONAL' THEN TRNDTE_10
                                 ELSE CURPRM_10
                             END AS fecha,
                             CASE
                                 WHEN origen_proveedor = 'NACIONAL' THEN TNXDTE_15
                                 ELSE TRNDTE_10
                             END AS fecha_entrega_real
                         FROM ORIGEN
                     ), DIAS AS (
                         SELECT *,
                             CASE
                                 -- Si es Viernes (6 en SQL Server default, depende de SET DATEFIRST)
                                 WHEN DATEPART(dw, fecha) = 6 THEN DATEDIFF(day, fecha_entrega_real, fecha) + @Espacio_tiempo + 2
                                 -- Si es Jueves (5)
                                 WHEN DATEPART(dw, fecha) = 5 THEN DATEDIFF(day, fecha_entrega_real, fecha) + @Espacio_tiempo + 1
                                 -- Si es menor a Jueves
                                 WHEN DATEPART(dw, fecha) < 5 THEN DATEDIFF(day, fecha_entrega_real, fecha) + @Espacio_tiempo
                                 ELSE NULL
                             END AS dias
                         FROM TABLA_FECHA
                     ), ZONA_NEGRA AS (
                         SELECT *,
                             CASE
                                 WHEN dias < 0 THEN 1 ELSE 0
                             END AS zona_negra
                         FROM DIAS
                     ), SKU AS (
                         SELECT proveedor, COUNT(ORDNUM_10) AS conteo
                         FROM ZONA_NEGRA
                         GROUP BY proveedor
                     ), ZONA_ROJA_AMARILLA AS (
                         SELECT *,
                             CASE
                                 -- Nota: 2.0 y 3.0 fuerzan la división decimal
                                 WHEN dias < (@Amortiguador - (2.0 / 3.0 * @Amortiguador)) AND dias >= 0 THEN 1 ELSE 0
                             END AS zona_roja,
                             CASE
                                 WHEN dias BETWEEN (@Amortiguador / 3.0) AND (2.0 / 3.0 * @Amortiguador) THEN 1 ELSE 0
                             END AS zona_amarilla,
                             CASE
                                 WHEN dias > (2.0 / 3.0 * @Amortiguador) THEN 1 ELSE 0
                             END AS zona_verde
                         FROM ZONA_NEGRA
                     ), RESUMEN_2 AS (
                         SELECT
                             *
                             ,ROW_NUMBER() OVER(PARTITION BY ORDER_10 ORDER BY TNXDTE_15 asc) AS N
                             ,MIN(TNXDTE_15) OVER(PARTITION BY ORDER_10) AS fecha_minima
                         FROM ZONA_ROJA_AMARILLA
                     ), TABLA_ACUMULADO_CATIDADA AS (
                         SELECT
                             *
                             , CASE WHEN TNXDTE_15=fecha_minima THEN 0 ELSE 1 END AS conteo
                             ,SUM(CANTIDAD_INGRESADA) OVER (PARTITION BY ORDER_10 ORDER BY TNXDTE_15) AS suma_acumulada_ciudad
                         FROM RESUMEN_2
                     ), CUMPLE_PRIMERA_ENTREGA AS (
                         SELECT
                             *
                             ,CASE WHEN conteo=0 AND suma_acumulada_ciudad>=CANTIDAD_PEDIDO
                                 THEN 'CUMPLE' ELSE 'NO CUMPLE' END AS CUMPLE_PRIMERA_ENTREGA
                         FROM TABLA_ACUMULADO_CATIDADA
                     ), tabla_final as (
                         SELECT
                             *
                             ,CASE WHEN N=1 and CUMPLE_PRIMERA_ENTREGA='CUMPLE' THEN 1 ELSE 0 END CUMPLE_1_ENTREGA
                             ,CASE WHEN N=1 and CUMPLE_PRIMERA_ENTREGA='NO CUMPLE' THEN 1 ELSE 0 END NO_CUMPLE_1_ENTREGA
                         FROM CUMPLE_PRIMERA_ENTREGA
                     ), TABLA_FINAL_ AS (
                         SELECT
                             ORDNUM_10
                             ,ORDER_10
                             ,PRTNUM_10
                             ,TNXDTE_15
                             ,TYPE_10
                             ,STATUS_10
                             ,TNXCDE_15
                             ,proveedor
                             ,CANTIDAD_INGRESADA
                             ,CANTIDAD_PEDIDO
                             ,origen_proveedor
                             ,fecha
                             ,fecha_entrega_real
                             ,ZONA_NEGRA
                             ,zona_roja
                             ,zona_amarilla
                             ,zona_verde
                             ,CUMPLE_1_ENTREGA
                             ,NO_CUMPLE_1_ENTREGA
                             ,N
                         FROM tabla_final
                     ), RESUMEN_UL AS (
                         SELECT
                             proveedor,
                             SUM(zona_negra) AS negra,
                             SUM(zona_roja) AS roja,
                             SUM(zona_amarilla) AS amarilla,
                             SUM(zona_verde) AS verde
                         FROM TABLA_FINAL_
                         WHERE origen_proveedor = 'NACIONAL'
                         GROUP BY proveedor
                     ), RESUMEN_F AS (
                         SELECT
                             RESUMEN_UL.proveedor,
                             SKU.conteo AS Total,
                             RESUMEN_UL.negra,
                             RESUMEN_UL.roja,
                             RESUMEN_UL.amarilla,
                             RESUMEN_UL.verde
                         FROM RESUMEN_UL
                         LEFT JOIN SKU
                             ON SKU.proveedor = RESUMEN_UL.proveedor
                     ), CANTIDAD_CUMPLIMIENTO AS (
                         SELECT
                             ORDER_10
                             ,proveedor
                             ,MAX(CANTIDAD_PEDIDO) AS cantidad_maxima
                             ,sum(cantidad_ingresada) as cantidad_ingresada
                             ,CASE WHEN sum(cantidad_ingresada)>=MAX(CANTIDAD_PEDIDO) THEN 1 ELSE 0 END AS 'CUMPLIO_CANTIDAD'
                             ,CASE WHEN sum(cantidad_ingresada)<MAX(CANTIDAD_PEDIDO) THEN 1 ELSE 0 END AS 'INCUMPLIO_CANTIDAD'
                         FROM TABLA_FINAL_
                         GROUP BY ORDER_10,proveedor
                     ), SUMA_CANTIDAD AS (
                         SELECT
                             proveedor
                             ,SUM(CUMPLIO_CANTIDAD) AS CUMPLIO_CANTIDAD
                             ,SUM(INCUMPLIO_CANTIDAD) AS INCUMPLIO_CANTIDAD
                         FROM CANTIDAD_CUMPLIMIENTO
                         GROUP BY proveedor
                     ), PORCENTAJE AS (
                         SELECT
                             RESUMEN_F.proveedor
                             ,RESUMEN_F.Total
                             ,RESUMEN_F.negra
                             ,RESUMEN_F.roja
                             ,RESUMEN_F.amarilla
                             ,RESUMEN_F.verde
                             ,CASE
                                 WHEN (COALESCE(CUMPLIO_CANTIDAD, 0) + COALESCE(INCUMPLIO_CANTIDAD, 0)) = 0 THEN 0
                                 ELSE ROUND(
                                     1.0 * COALESCE(CUMPLIO_CANTIDAD, 0) /
                                     (COALESCE(CUMPLIO_CANTIDAD, 0) + COALESCE(INCUMPLIO_CANTIDAD, 0)), 3)
                             END AS porcentaje_cumplimiento_cantidad
                         FROM RESUMEN_F
                         LEFT JOIN SUMA_CANTIDAD
                             ON RESUMEN_F.proveedor=SUMA_CANTIDAD.proveedor



                     ), tabla_kpi as (

                     /* ==========================================================
                        3. SALIDA FINAL
                        ========================================================== */
                     SELECT
                         proveedor
                         ,Total
                         ,negra
                         ,roja
                         ,amarilla
                         ,verde
                         ,CAST(porcentaje_cumplimiento_cantidad AS DECIMAL(10,3)) AS '%cumplimiento_cantidad'
                         ,CAST( (CAST(verde AS DECIMAL(18, 4)) / NULLIF(Total, 0)) * 100 AS DECIMAL(18, 2) ) AS Porcentaje
                     	,100-CAST( (CAST(negra AS DECIMAL(18, 4)) / NULLIF(Total, 0)) * 100 AS DECIMAL(18, 2) ) AS cumplimiento_entrega

                     FROM PORCENTAJE


                     )

                     SELECT
                     *
                     ,MONTH(GETDATE()) AS mes
            ,YEAR(GETDATE()) AS Anio


                     FROM tabla_kpi

                     ORDER BY Porcentaje DESC;
                 """, nativeQuery = true)

    List<consulta> getAllConsulta();
}
