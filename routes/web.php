<?php

use Illuminate\Support\Facades\Route;
use App\Http\Controllers\AlertsController;

/*
|--------------------------------------------------------------------------
| Web Routes
|--------------------------------------------------------------------------
|
| Here is where you can register web routes for your application. These
| routes are loaded by the RouteServiceProvider within a group which
| contains the "web" middleware group. Now create something great!
|
*/

Route::get('/', function () {
    return view('welcome');
});

// Route displaying the alerts in real time thanks to the websocket
Route::get('/processus', [AlertsController::class, 'websocket']);

// Route displaying all the alerts in the DB
Route::get('/alerts', [AlertsController::class, 'index']);

// Route displaying a specific alert
Route::get('/alerts/{alert}', [AlertsController::class, 'show']);

// Route displaying the form to edit (thus update/add a status) an alert
Route::get('/alerts/{alert}/edit', [AlertsController::class, 'edit']);

// Route updating the status of an alert
Route::put('/alerts/{alert}', [AlertsController::class, 'update']);

// Route deleting an alert
Route::delete('/alerts/{alert}', [AlertsController::class, 'destroy']);
