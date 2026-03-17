
import requests
import pandas as pd



def obtener_datos_kpi():
    url_api = "http://localhost:8080/api/v1/extractor/consulta"

    respuesta = requests.get(url_api)
    pd.set_option('display.max_columns', None) # Muestra todas las columnas
    pd.set_option('display.max_rows', None)    # Muestra todas las filas
    pd.set_option('display.width', 1000)       # Amplía el ancho de la consola para que no se rompa el texto

    if respuesta.status_code == 200:
        # 2. Obtenemos el JSON limpio que tu Service en Java preparó
        datos_proveedores = respuesta.json()
        print("cargo el api rest")
        df=pd.DataFrame(datos_proveedores)
        df=df[["proveedor","verde","amarilla","roja","negra","total","cumplimientoEntrega","mes","anio"]]
        df=df.sort_values("cumplimientoEntrega",ascending=True)
        return df
    else:
        print("no cargo api rest")

        return None


if __name__ == "__main__":
    print(obtener_datos_kpi())



