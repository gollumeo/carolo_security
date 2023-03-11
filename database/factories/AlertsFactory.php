<?php

namespace Database\Factories;

use Illuminate\Database\Eloquent\Factories\Factory;

/**
 * @extends \Illuminate\Database\Eloquent\Factories\Factory<\App\Models\Alerts>
 */
class AlertsFactory extends Factory
{
    /**
     * Define the model's default state.
     *
     * @return array<string, mixed>
     */
    public function definition()
    {
        return [
            'type' => "trafic accident",
            'description' => $this->faker->sentence(),
            'status' => "closed",
            'confidence' => $this->faker->randomFloat(2, 0.01, 1.00),
            'img_src' => $this->faker->imageUrl(),
            'camera_id' => "1",
        ];
    }
}
