#import "com_codename1_braintree_impl_BraintreeNativeImpl.h"
#import "BraintreeCore.h"
#import "BraintreeDropIn.h"
#include "CodenameOne_GLViewController.h"
#include "com_codename1_braintree_impl_BraintreePaymentCallback.h"

@implementation com_codename1_braintree_impl_BraintreeNativeImpl

-(void)showChargeUI:(NSString*)param{
    dispatch_async(dispatch_get_main_queue(), ^{
        BTDropInRequest *request = [[BTDropInRequest alloc] init];
        BTDropInController *dropIn = [[BTDropInController alloc] initWithAuthorization:param request:request handler:^(BTDropInController * _Nonnull controller, BTDropInResult * _Nullable result, NSError * _Nullable error) {
            
            if (error != nil) {
                NSLog(@"ERROR %@", [error localizedDescription]);
                com_codename1_braintree_impl_BraintreePaymentCallback_onPurchaseFail___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG [error localizedDescription]));
            } else if (result.cancelled) {
                NSLog(@"CANCELLED");
                com_codename1_braintree_impl_BraintreePaymentCallback_onPurchaseCancel__(CN1_THREAD_GET_STATE_PASS_SINGLE_ARG);
            } else {
                NSLog(@"Success Nonce: %@", result.paymentMethod.nonce);                
                com_codename1_braintree_impl_BraintreePaymentCallback_onPurchaseSuccess___java_lang_String(CN1_THREAD_GET_STATE_PASS_ARG fromNSString(CN1_THREAD_GET_STATE_PASS_ARG result.paymentMethod.nonce));
            }
            [controller dismissViewControllerAnimated:YES completion:nil];
        }];
        [[CodenameOne_GLViewController instance] presentViewController:dropIn animated:YES completion:nil];
    });
}

-(BOOL)isSupported{
    return YES;
}

@end
