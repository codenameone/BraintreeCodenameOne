# Braintree CodenameOne

Integrates the Braintree SDK's for iOS/Android. There is also a somewhat broken fallback implementation based on the  JavaScript API. This SDK assumes you integrated braintree in the server and have a server side API to facilitate the payment processing.

Check out the [Braintree's docs](https://developers.braintreepayments.com/start/overview) to understand how this integrates with the server side which does most of the heavy lifting within this API. 

The API for Braintree on the client includes one method: `Purchase.startOrder()` where a callback interface is passed to provide:

````java
Purchase.startOrder(new Purchase.Callback() {
        public String fetchToken() {
           // this method needs to return the token from the Brain tree server API. 
           // You need to use this code to connect to your server or return the data
           // from a previous connection that fetched the token
        }
        
        public void onPurchaseSuccess(String nonce) {
            // this is a callback that will be invoked when the purchase succeeds
        }
        
        public void onPurchaseFail(String errorMessage) {
            // this is a callback that will be invoked when the purchase fails
        }
        
        public void onPurchaseCancel() {
            // this is a callback that will be invoked when the purchase is canceled
        }
    });
````
