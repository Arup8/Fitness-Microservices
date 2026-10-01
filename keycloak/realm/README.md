Put your exported Keycloak realm JSON here (e.g. `fitness-app-realm.json`).
Keycloak imports every `*.json` in this folder on first start (`--import-realm`).

Export: Keycloak admin console -> realm `fitness-app` -> Realm settings -> Action -> Partial export
(tick "Include groups and roles" and "Include clients").
