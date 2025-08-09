# Script de test complet pour BBBrewery API
Write-Host "=== Test complet de l'API BBBrewery ===" -ForegroundColor Green

$baseUrl = "http://localhost:8080"

# Fonction pour tester un endpoint avec gestion d'erreurs amelioree
function Test-Endpoint {
    param(
        [string]$url,
        [string]$description,
        [string]$method = "GET"
    )

    Write-Host "`n--- $description ---" -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri $url -Method $method -TimeoutSec 30
        Write-Host "Success!" -ForegroundColor Green

        if ($response -is [array] -and $response.Count -gt 0) {
            Write-Host "Resultats trouves: $($response.Count) elements" -ForegroundColor Cyan
            $response[0] | ConvertTo-Json -Depth 3
        } elseif ($response -is [hashtable] -or $response -is [pscustomobject]) {
            $response | ConvertTo-Json -Depth 3
        } else {
            Write-Host "Reponse: $response" -ForegroundColor White
        }
    }
    catch {
        $statusCode = $_.Exception.Response.StatusCode.value__
        if ($statusCode -eq 404) {
            Write-Host "Endpoint non trouve (404) - Normal si pas encore implemente" -ForegroundColor Yellow
        } elseif ($statusCode -eq 500) {
            Write-Host "Erreur serveur (500): $($_.Exception.Message)" -ForegroundColor Red
        } else {
            Write-Host "Erreur ($statusCode): $($_.Exception.Message)" -ForegroundColor Red
        }
    }
}

Write-Host "`nTESTS DE CONNECTIVITE" -ForegroundColor Magenta

# Tests de base
Test-Endpoint "$baseUrl/api/test/ping" "Ping API"
Test-Endpoint "$baseUrl/api/test/db-connection" "Connexion base de donnees"
Test-Endpoint "$baseUrl/api/test/tables" "Verification tables"
Test-Endpoint "$baseUrl/api/test/system-info" "Informations systeme"

Write-Host "`nTESTS DES ENDPOINTS METIER" -ForegroundColor Magenta

# Tests des endpoints principaux
Test-Endpoint "$baseUrl/api/products" "Produits"
Test-Endpoint "$baseUrl/api/baskets" "Paniers"
Test-Endpoint "$baseUrl/api/shoppers" "Clients"

Write-Host "`nTESTS DES RAPPORTS" -ForegroundColor Magenta

# Tests des rapports
Test-Endpoint "$baseUrl/api/reports/dashboard" "Dashboard"
Test-Endpoint "$baseUrl/api/reports/stock" "Rapport de stock"

Write-Host "`nTESTS DE LIVRAISON" -ForegroundColor Magenta

# Tests de livraison
Test-Endpoint "$baseUrl/api/shipping" "Livraisons"

Write-Host "`nTESTS DES TAXES" -ForegroundColor Magenta

# Tests des taxes
Test-Endpoint "$baseUrl/api/tax/states" "Etats avec taxes"
Test-Endpoint "$baseUrl/api/tax/rates" "Taux de taxes"

Write-Host "`nTESTS SPECIFIQUES" -ForegroundColor Magenta

# Tests avec parametres
Test-Endpoint "$baseUrl/api/tax/rate/state/CA" "Taux de taxe Californie"
Test-Endpoint "$baseUrl/api/reports/statistics/count-by-status/ACTIVE" "Statistiques paniers actifs"

Write-Host "`nRESUME DES TESTS" -ForegroundColor Green
Write-Host "Tests de connectivite termines" -ForegroundColor Green
Write-Host "Tests des endpoints metier termines" -ForegroundColor Green
Write-Host "Tests des rapports termines" -ForegroundColor Green
Write-Host "Tests de livraison termines" -ForegroundColor Green
Write-Host "Tests des taxes termines" -ForegroundColor Green

Write-Host "`nPROCHAINES ETAPES:" -ForegroundColor Cyan
Write-Host "1. Verifiez les endpoints qui retournent 404" -ForegroundColor White
Write-Host "2. Corrigez les erreurs 500 si il y en a" -ForegroundColor White
Write-Host "3. Ajoutez des donnees de test si les endpoints sont vides" -ForegroundColor White
Write-Host "4. Testez les operations CRUD (POST, PUT, DELETE)" -ForegroundColor White

Write-Host "`nTests termines" -ForegroundColor Green