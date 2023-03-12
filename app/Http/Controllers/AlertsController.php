<?php

namespace App\Http\Controllers;

use App\Http\Requests\AlertRequest;
use App\Models\Alerts;
use Illuminate\Http\Request;

// TODO: add a function to use and connect to the websocket and store the new alert in DB.

class AlertsController extends Controller
{
    /**
     * Display a listing of the resource.
     *
     * @return \Illuminate\Contracts\Foundation\Application|\Illuminate\Contracts\View\Factory|\Illuminate\Contracts\View\View
     */
    public function index()
    {
        return view('alerts.index', ['alerts' => Alerts::all()]);
    }

    /**
     * Show the form for creating a new resource.
     *
     * @return \Illuminate\Http\Response
     */
    public function create()
    {
        // will not be used given we'll be using websockets
    }

    /**
     * Store a newly created resource in storage.
     *
     * @param  \Illuminate\Http\Request  $request
     * @return \Illuminate\Http\Response
     */
    public function store(Request $request)
    {
        // TODO ONCE WE HAVE THE WEBSOCKETS
    }

    /**
     * Display the specified resource.
     *
     * @param  \App\Models\Alerts  $alerts
     * @return \Illuminate\Contracts\Foundation\Application|\Illuminate\Contracts\View\Factory|\Illuminate\Contracts\View\View
     */
    public function show(Alerts $alerts)
    {
        return view('alerts.show', ['alert' => $alerts]);
    }

    /**
     * Show the form for editing the specified resource.
     *
     * @param  \App\Models\Alerts  $alerts
     * @return \Illuminate\Contracts\Foundation\Application|\Illuminate\Contracts\View\Factory|\Illuminate\Contracts\View\View
     */
    public function edit(Alerts $alerts)
    {
        return view('alerts.edit', ['alert' => $alerts]);
    }

    /**
     * Update the specified resource in storage.
     *
     * @param  \Illuminate\Http\Request  $request
     * @param  \App\Models\Alerts  $alerts
     * @return \Illuminate\Contracts\Foundation\Application|\Illuminate\Http\RedirectResponse|\Illuminate\Routing\Redirector
     */
    public function update(AlertRequest $request, $id)
    {
        $edited_alert = Alerts::find($id);
        return $this->validate_status($request, $edited_alert);
    }

    /**
     * Remove the specified resource from storage.
     *
     * @param  \App\Models\Alerts  $alerts
     * @return \Illuminate\Http\Response
     */
    public function destroy(Alerts $alerts)
    {
        $alerts = Alerts::find($alerts);
        $alerts->delete();
    }

    public function validate_status(AlertRequest $request, $alert)
    {
        $alert->type = $request->input('type');
        $alert->description = $request->input('description');
        $alert->status = $request->input('status');
        $alert->confidence = $request->input('confidence');
        $alert->img_src =  $request->input('img_src');
        $alert->camera_id = $request->input('camera_id');
        $alert->save();

        return redirect('/alerts');
    }

    public function websocket()
    {
        // TODO: implement websocket
    }
}
