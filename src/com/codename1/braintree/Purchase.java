package com.codename1.braintree;

import com.codename1.braintree.impl.BraintreeNative;
import com.codename1.braintree.impl.BraintreePaymentCallback;
import com.codename1.components.InfiniteProgress;
import com.codename1.components.ToastBar;
import com.codename1.io.ConnectionRequest;
import com.codename1.io.Log;
import com.codename1.io.NetworkManager;
import com.codename1.javascript.JSFunction;
import com.codename1.javascript.JSObject;
import com.codename1.javascript.JavascriptContext;
import com.codename1.system.NativeLookup;
import com.codename1.ui.BrowserComponent;
import com.codename1.ui.Component;
import com.codename1.ui.Dialog;
import com.codename1.ui.Display;
import com.codename1.ui.FontImage;
import com.codename1.ui.Form;
import com.codename1.ui.events.ActionEvent;
import com.codename1.ui.layouts.BorderLayout;
import com.codename1.ui.layouts.FlowLayout;

/**
 * It's not good practice to call the native interface directly, this class 
 * hides some of the low level implementation details if any.
 */
public class Purchase {
    static boolean flag;
    
    public static interface Callback {
        public String fetchToken();
        public void onPurchaseSuccess(String nonce);
        public void onPurchaseFail(String errorMessage);
        public void onPurchaseCancel();
    }
    
    public static void startOrder(Callback callback) {        
        // fetch token from the server
        BraintreePaymentCallback.cb = callback;
        String token = callback.fetchToken();
        
        BraintreeNative bn = NativeLookup.create(BraintreeNative.class);
        if(bn != null && bn.isSupported()) {
            bn.showChargeUI(token);
        } else {
            // default to using the JavaScript gateway...
            Form buy = new Form(new BorderLayout());
            final Form previous = Display.getInstance().getCurrent();
            buy.getToolbar().addMaterialCommandToLeftBar("", FontImage.MATERIAL_CANCEL, e -> previous.showBack());
            final BrowserComponent cmp = new BrowserComponent();
            buy.add(BorderLayout.CENTER, cmp);
            
            cmp.addWebEventListener("onLoad", e -> {
                JavascriptContext ctx = new JavascriptContext(cmp);
                    JSObject jsProxy = (JSObject)ctx.get("{}");
                    jsProxy.set("onSuccessNonce", new JSFunction() {
                        public void apply(JSObject self, Object[] args) {
                            BraintreePaymentCallback.onPurchaseSuccess((String)args[0]);
                        }
                    });
                    jsProxy.set("onErrorCall", new JSFunction() {
                        public void apply(JSObject self, Object[] args) {
                            BraintreePaymentCallback.onPurchaseFail((String)args[0]);
                        }
                    });
                    jsProxy.set("onCancelCall", new JSFunction() {
                        public void apply(JSObject self, Object[] args) {
                            previous.showBack();
                        }
                    });
            });
            
            cmp.setPage("<html><head>" +
                "    <style type=\"text/css\">\n" +
                "        html, body {\n" +
                "            height: 100%;\n" +
                "            padding: 0;\n" +
                "            margin: 0;     \n" +
                "        }\n" +
                "        \n" +
                "        /* Dummy CSS to fix bug in JavaFX webview that caused gibberish display */\n" +
                "        .gm-style-mtc > div, .gm-style > div, .gm-style-cc > div, .gm-style {font-family:sans-serif !important;}\n" +
                "    </style>" +
                "</head><body>" +
                "\n<form id=\"checkout-form\" action=\"/transaction-endpoint\" method=\"post\">\n" +
                "  <div id=\"error-message\"></div>\n" +
                "\n" +
                "  <label for=\"card-number\">Card Number</label>\n" +
                "  <div class=\"hosted-field\" id=\"card-number\"></div>\n" +
                "\n" +
                "  <label for=\"cvv\">CVV</label>\n" +
                "  <div class=\"hosted-field\" id=\"cvv\"></div>\n" +
                "\n" +
                "  <label for=\"expiration-date\">Expiration Date</label>\n" +
                "  <div class=\"hosted-field\" id=\"expiration-date\"></div>\n" +
                "\n" +
                "  <input type=\"hidden\" name=\"payment_method_nonce\">\n" +
                "  <input type=\"submit\" value=\"Pay\" disabled>\n" +
                "</form>\n" +
                "\n" +
                "<!-- Load the Client component. -->\n" +
                "<script src=\"https://js.braintreegateway.com/web/3.11.1/js/client.min.js\"></script>\n" +
                "\n" +
                "<!-- Load the Hosted Fields component. -->\n" +
                "<script src=\"https://js.braintreegateway.com/web/3.11.1/js/hosted-fields.min.js\"></script>\n" +
                "\n" +
                "<script>\n" +
                "var authorization = \"" + token + "\";\n" +
                "var submit = document.querySelector('input[type=\"submit\"]');\n" +
                "\n" +
                "braintree.client.create({\n" +
                "  authorization: authorization\n" +
                "}, function (clientErr, clientInstance) {\n" +
                "  if (clientErr) {\n" +
                "    // Handle error in client creation\n" +
                "    onErrorCall(clientErr.toString());" +
                "    return;\n" +
                "  }\n" +
                "\n" +
                "  braintree.hostedFields.create({\n" +
                "    client: clientInstance,\n" +
                "    styles: {\n" +
                "      'input': {\n" +
                "        'font-size': '14pt'\n" +
                "      },\n" +
                "      'input.invalid': {\n" +
                "        'color': 'red'\n" +
                "      },\n" +
                "      'input.valid': {\n" +
                "        'color': 'green'\n" +
                "      }\n" +
                "    },\n" +
                "    fields: {\n" +
                "      number: {\n" +
                "        selector: '#card-number',\n" +
                "        placeholder: '4111 1111 1111 1111'\n" +
                "      },\n" +
                "      cvv: {\n" +
                "        selector: '#cvv',\n" +
                "        placeholder: '123'\n" +
                "      },\n" +
                "      expirationDate: {\n" +
                "        selector: '#expiration-date',\n" +
                "        placeholder: '10/2019'\n" +
                "      }\n" +
                "    }\n" +
                "  }, function (hostedFieldsErr, hostedFieldsInstance) {\n" +
                "    if (hostedFieldsErr) {\n" +
                "      // Handle error in Hosted Fields creation\n" +
                "      return;\n" +
                "    }\n" +
                "\n" +
                "    submit.removeAttribute('disabled');\n" +
                "  });\n" +
                "});\n" +
                "var form = document.querySelector('#checkout-form');\n" +
                "var submit = document.querySelector('input[type=\"submit\"]');\n" +
                "\n" +
                "braintree.client.create({\n" +
                "  // Replace this with your own authorization.\n" +
                "  authorization: 'eyJ2ZXJzaW9uIjoyLCJhdXRob3JpemF0aW9uRmluZ2VycHJpbnQiOiJiYmIxZTc0ODhhZDJhZjBlMzhhOWFmMzYzYjAxMTJkOGJmMjRlMWFlNThmMDVhMDhjZDk5ODM5NGI1ODhkMTMxfGNyZWF0ZWRfYXQ9MjAxNy0wMy0yOVQxMzo0MDoyOC41MTc2MzM1OTgrMDAwMFx1MDAyNm1lcmNoYW50X2lkPTM0OHBrOWNnZjNiZ3l3MmJcdTAwMjZwdWJsaWNfa2V5PTJuMjQ3ZHY4OWJxOXZtcHIiLCJjb25maWdVcmwiOiJodHRwczovL2FwaS5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tOjQ0My9tZXJjaGFudHMvMzQ4cGs5Y2dmM2JneXcyYi9jbGllbnRfYXBpL3YxL2NvbmZpZ3VyYXRpb24iLCJjaGFsbGVuZ2VzIjpbXSwiZW52aXJvbm1lbnQiOiJzYW5kYm94IiwiY2xpZW50QXBpVXJsIjoiaHR0cHM6Ly9hcGkuc2FuZGJveC5icmFpbnRyZWVnYXRld2F5LmNvbTo0NDMvbWVyY2hhbnRzLzM0OHBrOWNnZjNiZ3l3MmIvY2xpZW50X2FwaSIsImFzc2V0c1VybCI6Imh0dHBzOi8vYXNzZXRzLmJyYWludHJlZWdhdGV3YXkuY29tIiwiYXV0aFVybCI6Imh0dHBzOi8vYXV0aC52ZW5tby5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tIiwiYW5hbHl0aWNzIjp7InVybCI6Imh0dHBzOi8vY2xpZW50LWFuYWx5dGljcy5zYW5kYm94LmJyYWludHJlZWdhdGV3YXkuY29tLzM0OHBrOWNnZjNiZ3l3MmIifSwidGhyZWVEU2VjdXJlRW5hYmxlZCI6dHJ1ZSwicGF5cGFsRW5hYmxlZCI6dHJ1ZSwicGF5cGFsIjp7ImRpc3BsYXlOYW1lIjoiQWNtZSBXaWRnZXRzLCBMdGQuIChTYW5kYm94KSIsImNsaWVudElkIjpudWxsLCJwcml2YWN5VXJsIjoiaHR0cDovL2V4YW1wbGUuY29tL3BwIiwidXNlckFncmVlbWVudFVybCI6Imh0dHA6Ly9leGFtcGxlLmNvbS90b3MiLCJiYXNlVXJsIjoiaHR0cHM6Ly9hc3NldHMuYnJhaW50cmVlZ2F0ZXdheS5jb20iLCJhc3NldHNVcmwiOiJodHRwczovL2NoZWNrb3V0LnBheXBhbC5jb20iLCJkaXJlY3RCYXNlVXJsIjpudWxsLCJhbGxvd0h0dHAiOnRydWUsImVudmlyb25tZW50Tm9OZXR3b3JrIjp0cnVlLCJlbnZpcm9ubWVudCI6Im9mZmxpbmUiLCJ1bnZldHRlZE1lcmNoYW50IjpmYWxzZSwiYnJhaW50cmVlQ2xpZW50SWQiOiJtYXN0ZXJjbGllbnQzIiwiYmlsbGluZ0FncmVlbWVudHNFbmFibGVkIjp0cnVlLCJtZXJjaGFudEFjY291bnRJZCI6ImFjbWV3aWRnZXRzbHRkc2FuZGJveCIsImN1cnJlbmN5SXNvQ29kZSI6IlVTRCJ9LCJjb2luYmFzZUVuYWJsZWQiOmZhbHNlLCJtZXJjaGFudElkIjoiMzQ4cGs5Y2dmM2JneXcyYiIsInZlbm1vIjoib2ZmIn0='\n" +
                "}, function (clientErr, clientInstance) {\n" +
                "  if (clientErr) {\n" +
                "    // Handle error in client creation\n" +
                "    return;\n" +
                "  }\n" +
                "\n" +
                "  braintree.hostedFields.create({\n" +
                "    client: clientInstance,\n" +
                "    styles: {\n" +
                "      'input': {\n" +
                "        'font-size': '14pt'\n" +
                "      },\n" +
                "      'input.invalid': {\n" +
                "        'color': 'red'\n" +
                "      },\n" +
                "      'input.valid': {\n" +
                "        'color': 'green'\n" +
                "      }\n" +
                "    },\n" +
                "    fields: {\n" +
                "      number: {\n" +
                "        selector: '#card-number',\n" +
                "        placeholder: '4111 1111 1111 1111'\n" +
                "      },\n" +
                "      cvv: {\n" +
                "        selector: '#cvv',\n" +
                "        placeholder: '123'\n" +
                "      },\n" +
                "      expirationDate: {\n" +
                "        selector: '#expiration-date',\n" +
                "        placeholder: '10/2019'\n" +
                "      }\n" +
                "    }\n" +
                "  }, function (hostedFieldsErr, hostedFieldsInstance) {\n" +
                "    if (hostedFieldsErr) {\n" +
                "      // Handle error in Hosted Fields creation\n" +
                "      return;\n" +
                "    }\n" +
                "\n" +
                "    submit.removeAttribute('disabled');\n" +
                "\n" +
                "    form.addEventListener('submit', function (event) {\n" +
                "      event.preventDefault();\n" +
                "\n" +
                "      hostedFieldsInstance.tokenize(function (tokenizeErr, payload) {\n" +
                "        if (tokenizeErr) {\n" +
                "          // Handle error in Hosted Fields tokenization\n" +
                "          return;\n" +
                "        }\n" +
                "\n" +
                "        onSuccessNonce(payload.nonce);\n" +
                "      });\n" +
                "    }, false);\n" +
                "  });\n" +
                "});\n"+
                "</script></body></html>", null);
            buy.show();
        }

        // this code is here to prevent the iOS VM from stripping out that class 
        // it might assume incorrectly that it's unused but it's invoked from native 
        // code... flag should always be false...
        if(flag) {
            BraintreePaymentCallback.onPurchaseCancel();
            BraintreePaymentCallback.onPurchaseFail(null);
            BraintreePaymentCallback.onPurchaseSuccess(null);
        }
    }
}
