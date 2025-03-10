# HDN Library
## How to use
After checking out the repository, place your P12 certificate file in the root directory of the repository.\
Next, create a settings.properties in the root directory and configure the values (see below for an example).\
Run mvn to build and/or test the project.

## Example settings.properties
```
baseURL=https://pot-gto.hdn.nl/api/v2
authURL=https://auth-gto.hdn.nl
certificate=[P12 certificate file]
password=[certificate password]
publickeyUUID=[UUID of the registered public key of the certificate]
senderNode=[Node number of the sender]
receiverNode=[Node number of the receiver]
clientID=[Client ID f.e. hdn-pot-client]
clientSecret=[Client secret]
```
