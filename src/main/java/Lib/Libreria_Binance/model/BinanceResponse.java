package Lib.Libreria_Binance.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class BinanceResponse {
    private final boolean acceso;
    private final int estado;
    private final String data;
    private final String mensaje;

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("BinanceResponse{\n");
        sb.append("  acceso=").append(acceso).append(",\n");
        sb.append("  estado=").append(estado).append(",\n");
        sb.append("  mensaje='").append(mensaje).append("',\n");
        sb.append("  data='").append(data != null ? data : "null").append("'\n");
        sb.append("}");
        return sb.toString();
    }

    // Método adicional para ver solo los datos de forma más limpia
    public void printDetails() {
        System.out.println("=== DETALLES DE LA RESPUESTA ===");
        System.out.println("Acceso exitoso: " + acceso);
        System.out.println("Código de estado: " + estado);
        System.out.println("Mensaje: " + mensaje);
        System.out.println("Datos recibidos:");
        if (data != null && !data.trim().isEmpty()) {
            System.out.println(data);
        } else {
            System.out.println("No hay datos en la respuesta");
        }
        System.out.println("===============================\n");
    }
}