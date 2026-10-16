package edu.rentcar.client;

import com.rentcar.paymentclient.PaymentApiClient;
import com.rentcar.paymentclient.PaymentApiRequest;
import com.rentcar.paymentclient.PaymentApiResponse;

import java.io.IOException;

/**
 Clase ejemplo de como llamar al sistema externo (payment-api.jar) el cual ya debe estarse ejecutando de acuerdo a
 las instrucciones del README (README-api-pagos.md) en la carpeta /external.

 La idea es que uds usen lo que necesitan de esta clase para adaptarlo a lo que se pide en el parcial para integrar
 los patrones necesarios.


 Esta clase solo es un ejemplo de como llamar a un sistema externo, la pueden eliminar si se desea.

 Lo que tienen que entregar es otra clase en alguna carpeta que use esta clase (PaymentApiClient) la cual sirve para
 llamar al sistema externo
 */

public class PaymentClientExample {

    public static void main(String[] args) {
        //Cliente para llamar al sistema externo payment-api.jar
        PaymentApiClient client = new PaymentApiClient("http://localhost:8080");

        //Datos de entrada del servicio del sistema externo
        PaymentApiRequest request = new PaymentApiRequest(
                "RES-1001",
                "CUS-1001",
                350000,
                "tok_approved" //Cambiar este campo para verificar si es approvado o no. Valores posibles (tok_approved, tok_declined, tok_api_error)
                                // Estos valores no los ponen uds sin un motivo. Deben analizar porque, para que y de donde debería salir el valor correcto
        );

        try {
            //Respuesta del sistema exerno
            PaymentApiResponse response = client.pay(request);

            System.out.println("HTTP Status: " + response.httpStatus());
            System.out.println("Response: " + response.body());

            //Se verifica la respuesta
            if (response.isApproved()) {
                System.out.println("Payment approved.");
            } else if (response.isDeclined()) {
                System.out.println("Payment declined.");
            } else {
                System.out.println("Payment could not be completed.");
            }
            //Manejo de errores
        } catch (IOException e) {
            System.err.println("Payment API communication error: "
                    + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Payment request was interrupted.");
        }
    }
}
