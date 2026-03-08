# Talos Legacy - Node Reference Guide

Talos is a node-based particle effect editor. Effects are built by connecting **modules** (nodes) together in a graph. Data flows from output slots to input slots through connections. Each particle emitter has two mandatory endpoint nodes: **Emitter Module** (controls spawning) and **Particle Module** (controls per-particle appearance and behavior).

---

## Table of Contents

- [Core Concepts](#core-concepts)
- [Endpoint Nodes](#endpoint-nodes)
  - [Emitter Module](#emitter-module)
  - [Particle Module](#particle-module)
- [Value Generators](#value-generators)
  - [Static Value](#static-value)
  - [Random Range](#random-range)
  - [Dynamic Range](#dynamic-range)
  - [Curve](#curve)
  - [Noise](#noise)
  - [Random Input](#random-input)
- [Color Nodes](#color-nodes)
  - [Color](#color)
  - [Gradient Color](#gradient-color)
- [Math & Logic](#math--logic)
  - [Math](#math)
  - [Mix (Lerp)](#mix-lerp)
  - [Interpolation](#interpolation)
- [Vector Operations](#vector-operations)
  - [Vector2](#vector2)
  - [Vector Split](#vector-split)
  - [Cartesian to Polar (CartToRad)](#cartesian-to-polar-carttorad)
  - [Polar to Cartesian (RadToCart)](#polar-to-cartesian-radtocart)
- [Position & Shape](#position--shape)
  - [Shape](#shape)
  - [Offset](#offset)
  - [From To](#from-to)
  - [Target](#target)
- [Forces & Physics](#forces--physics)
  - [Force Applier](#force-applier)
  - [Attractor](#attractor)
  - [Vector Field](#vector-field)
- [Drawables & Rendering](#drawables--rendering)
  - [Texture](#texture)
  - [Flipbook](#flipbook)
  - [Nine Patch](#nine-patch)
  - [Shader Material](#shader-material)
  - [Shaded Sprite (Deprecated)](#shaded-sprite-deprecated)
  - [Polyline](#polyline)
  - [Ribbon](#ribbon)
- [Input & Scope](#input--scope)
  - [Input](#input)
  - [Global Scope](#global-scope)
  - [Emitter Config](#emitter-config)
- [Utility](#utility)
  - [Fake Motion Blur](#fake-motion-blur)
  - [NAN (Placeholder)](#nan-placeholder)

---

## Core Concepts

### Value Types

| Type | Description |
|------|-------------|
| **NumericalValue** | A numeric value with 1-4 elements (scalar, 2D vector, 3D vector, or RGBA). Most common type. |
| **DrawableValue** | A reference to a renderable texture/sprite/shader. Used for particle visuals. |
| **EmConfigValue** | A set of boolean flags that configure emitter behavior. |

### Scope Variables

The runtime maintains a **scope** — a set of context variables available to all nodes during processing. These are automatically updated each frame:

| Key | ID | Description |
|-----|----|-------------|
| EMITTER_ALPHA | 0 | Current emitter lifetime progress (0-1) |
| PARTICLE_ALPHA | 1 | Current particle lifetime progress (0-1) |
| PARTICLE_SEED | 2 | Deterministic random seed unique to each particle |
| REQUESTER_ID | 3 | ID of the requesting entity |
| EMITTER_ALPHA_AT_P_INIT | 4 | Emitter alpha at the moment this particle was spawned |
| DRAWABLE_ASPECT_RATIO | 5 | Aspect ratio of the current particle drawable |
| POINT_ALPHA | 6 | Interpolation alpha (0-1) for the current point in ribbon/polyline rendering |
| TOTAL_TIME | 7 | Total elapsed time since effect start |
| PARTICLE_POSITION | 8 | Current particle world position |

Many nodes that accept an **alpha** input will automatically fall back to `PARTICLE_ALPHA` or `EMITTER_ALPHA` from scope if no input is connected.

### How Connections Work

- Each node has **input slots** (left side) and **output slots** (right side).
- You connect an output of one node to an input of another by dragging a wire between them.
- Data flows left-to-right through the graph.
- Each input can receive one connection. Each output can feed multiple inputs.
- If an input is not connected, the node uses its **default value** (configured in the node's properties panel).

---

## Endpoint Nodes

### Emitter Module

> Controls how and when particles are spawned.

Every emitter requires exactly one Emitter Module. It defines the timing and rate of particle emission.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Delay** | NumericalValue | `0` | Seconds to wait before emission starts. |
| **Duration** | NumericalValue | `2` | How long the emitter spawns particles (seconds). |
| **Rate** | NumericalValue | `50` | Number of particles spawned per second. |
| **Config** | EmConfigValue | *(see below)* | Emitter configuration flags. Connect an [Emitter Config](#emitter-config) node here. |

#### Outputs

None. This is an endpoint node.

---

### Particle Module

> Defines all visual and physical properties of each particle.

Every emitter requires exactly one Particle Module. All particle attributes are gathered here.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Drawable** | DrawableValue | *white pixel* | The texture/sprite to render for this particle. Connect a [Texture](#texture), [Flipbook](#flipbook), [Polyline](#polyline), [Ribbon](#ribbon), [Nine Patch](#nine-patch), or [Shader Material](#shader-material) node. |
| **Offset** | NumericalValue | `(0, 0)` | Initial spawn position offset from emitter origin. |
| **Life** | NumericalValue | `1.0` | Particle lifetime in seconds. |
| **Velocity** | NumericalValue | `0` | Movement speed (units/second). |
| **Gravity** | NumericalValue | `(0, 0)` | Gravity acceleration vector. |
| **Rotation** | NumericalValue | `0` | Visual rotation of the particle sprite (degrees). |
| **Target** | NumericalValue | `(0, 0)` | Target position for guided particles. |
| **Color** | NumericalValue | `(1, 1, 1)` | RGB color tint. Connect a [Color](#color) or [Gradient Color](#gradient-color) node. |
| **Transparency** | NumericalValue | `1.0` | Alpha transparency (0 = invisible, 1 = opaque). |
| **Angle** | NumericalValue | `0` | Launch/movement angle (degrees). |
| **Mass** | NumericalValue | `1` | Particle mass (used by physics modules). |
| **Size** | NumericalValue | `(1, 1)` | Scale of the particle. 1 element = uniform, 2 elements = (width, height). |
| **Position** | NumericalValue | *(none)* | Override the particle's current world position. |
| **Pivot** | NumericalValue | `(0.5, 0.5)` | Sprite pivot/anchor point (0-1 normalized). `(0.5, 0.5)` = center. |

#### Outputs

None. This is an endpoint node.

---

## Value Generators

### Static Value

> Outputs a constant, unchanging number.

The simplest node. Use it to provide a fixed value to any numerical input.

#### Inputs

None.

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | The constant value. |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Value | `1.0` | The static number to output. |

#### Typical Uses
- Set a fixed particle size, lifetime, velocity, or transparency.
- Provide a constant to a [Math](#math) operation.

---

### Random Range

> Generates a random value between min and max, seeded per-particle.

Each particle gets a deterministic random value within the range. The same particle always gets the same value (seeded by particle seed).

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Min** | NumericalValue | `1.0` | Minimum value of the range. |
| **Max** | NumericalValue | `1.0` | Maximum value of the range. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | Random value: `min + (max - min) * random` |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Min | `1.0` | Default minimum when input is not connected. |
| Max | `1.0` | Default maximum when input is not connected. |
| Distributed | `false` | When `true`, uses distributed random for more even spread. |

#### Typical Uses
- Randomize particle size, lifetime, velocity, or rotation.
- Create natural variation between particles.

---

### Dynamic Range

> Combines a curve with randomized min/max ranges for complex value animation.

Extends [Curve](#curve) by adding two random ranges (low and high). The curve output is used to interpolate between a random low value and a random high value over time.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Alpha** | NumericalValue | *particle alpha* | Position on the curve (0-1). Defaults to particle lifetime progress. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | `lerp(randomLow, randomHigh, curveValue)` |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Low Min | `0` | Minimum of the low range. |
| Low Max | `0` | Maximum of the low range. |
| High Min | `1` | Minimum of the high range. |
| High Max | `1` | Maximum of the high range. |
| Curve Points | `[(0, 0.5)]` | Editable curve keyframes (same as [Curve](#curve)). |

#### Typical Uses
- Animate particle size from a random small size to a random large size over lifetime.
- Create fade-in/fade-out transparency with per-particle variation.
- Any property that needs both animation over time AND randomized variation.

---

### Curve

> Maps an alpha value (0-1) to a custom curve shape.

Define keyframe points to create a custom value curve. The output is interpolated linearly between points.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Alpha** | NumericalValue | *particle alpha* | X position on the curve (0-1). Defaults to particle lifetime progress. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | Y value at the given X position on the curve (0-1). |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Curve Points | `[(0, 0.5)]` | Array of (x, y) keyframes. Both x and y are clamped to 0-1. Points are auto-sorted by x. |

#### Typical Uses
- Create ease-in/ease-out size or transparency over particle lifetime.
- Define custom timing curves for any animated property.

---

### Noise

> Generates Simplex noise from X/Y coordinates.

Produces smooth, organic-looking random values based on 2D coordinates. Useful for natural-looking variation.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **X** | NumericalValue | `0` | X coordinate for noise sampling. |
| **Y** | NumericalValue | `0` | Y coordinate for noise sampling. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | Simplex noise value at (X, Y). |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Frequency | `20` | Scale/frequency of the noise pattern. Higher = more detail. |

#### Typical Uses
- Organic wobble or turbulence on particle position/rotation.
- Procedural variation that changes smoothly in space.

---

### Random Input

> Randomly selects one of multiple connected inputs per particle.

Dynamically creates input slots as you connect nodes. Each particle randomly picks one of the connected inputs to use.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Input 0, 1, 2...** | *any* | *(none)* | Multiple inputs of the same type. New slots appear as you connect. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | *same as inputs* | The value from the randomly selected input. |

#### Typical Uses
- Randomly choose between multiple textures for variety.
- Pick from several color options per particle.

---

## Color Nodes

### Color

> Combines separate R, G, B channels into a single color output.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **R** | NumericalValue | `1.0` | Red channel (0-1). |
| **G** | NumericalValue | `0.0` | Green channel (0-1). |
| **B** | NumericalValue | `0.0` | Blue channel (0-1). |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | RGB color as 3-element vector. |

#### Typical Uses
- Build a color from animated or computed R/G/B channels.
- Connect to the **Color** input of [Particle Module](#particle-module).

---

### Gradient Color

> Interpolates colors along a gradient based on alpha position.

Define color keyframes along a gradient strip. The output color is interpolated based on the alpha input.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Alpha** | NumericalValue | *particle alpha* | Position in the gradient (0-1). Defaults to particle lifetime progress. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | RGBA color (4-element vector) at the given gradient position. |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Color Points | `[(0, orange #FF441A)]` | Array of `(position, color)` keyframes along the gradient (0-1). |

#### Typical Uses
- Animate particle color over lifetime (e.g., yellow -> orange -> red for fire).
- Create color transitions based on any alpha source.

---

## Math & Logic

### Math

> Performs a mathematical operation on two values.

Applies a selectable math expression to inputs A and B.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **A** | NumericalValue | `0` | First operand. |
| **B** | NumericalValue | `0` | Second operand. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | Result of the operation. |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Expression | `sum` | The math operation to apply. |

#### Available Expressions

| Expression | Formula | Description |
|------------|---------|-------------|
| **sum** | `A + B` | Addition |
| **substract** | `A - B` | Subtraction |
| **multiply** | `A * B` | Multiplication |
| **divide** | `A / B` | Division |
| **sin** | `sin(A) * B` | Sine of A, scaled by B |
| **cos** | `cos(A) * B` | Cosine of A, scaled by B |
| **pow** | `A ^ B` | A raised to the power of B |
| **abs** | `|A|` | Absolute value of A (B is ignored) |

#### Typical Uses
- Scale a value: multiply a curve output by a static value.
- Combine forces: add two velocity vectors.
- Create oscillation: use sin/cos with time input.

---

### Mix (Lerp)

> Linearly interpolates between two values based on alpha.

Blends smoothly between Val1 and Val2. Works element-wise on vectors.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Alpha** | NumericalValue | `0` | Blend factor (0 = Val1, 1 = Val2). |
| **Val1** | NumericalValue | `0` | Value at alpha = 0. |
| **Val2** | NumericalValue | `0` | Value at alpha = 1. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | `lerp(Val1, Val2, alpha)` |

#### Typical Uses
- Blend between two sizes, colors, or positions over time.
- Smoothly transition any property based on a control value.

---

### Interpolation

> Applies an easing function to an alpha value.

Converts a linear 0-1 input into a curved 0-1 output using standard easing functions (from libGDX).

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Alpha** | NumericalValue | `0` | Linear input value (0-1). |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | Eased/curved output value (0-1). |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Interpolation | `linear` | The easing curve to apply. |

#### Available Interpolations

All libGDX `Interpolation` types are available, including:
`linear`, `fade`, `pow2`, `pow3`, `pow4`, `pow5`, `sine`, `exp5`, `exp10`, `circle`, `elastic`, `swing`, `bounce`, `slowFast`, `fastSlow`, and their `In`/`Out` variants.

#### Typical Uses
- Add easing to particle size or transparency animations.
- Convert linear lifetime alpha into smooth ease-in/ease-out.

---

## Vector Operations

### Vector2

> Combines two scalars into a 2D vector.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **X** | NumericalValue | `0` | X component. |
| **Y** | NumericalValue | `0` | Y component. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | 2-element vector `(X, Y)`. |

#### Typical Uses
- Construct a position, size, or gravity vector from separate components.
- Combine separate X and Y animations into a single vector.

---

### Vector Split

> Decomposes a vector into its individual X, Y, Z components.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Input** | NumericalValue | `0` | Multi-element vector to split. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **X** | NumericalValue | First element of the vector. |
| **Y** | NumericalValue | Second element of the vector. |
| **Z** | NumericalValue | Third element of the vector. |

#### Typical Uses
- Extract individual color channels from a color value.
- Get X or Y from a position vector for separate processing.

---

### Cartesian to Polar (CartToRad)

> Converts a 2D position (X, Y) to polar coordinates (angle, length).

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Position** | NumericalValue | `(0, 0)` | 2D Cartesian coordinate. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Angle** | NumericalValue | Angle in degrees. |
| **Length** | NumericalValue | Distance from origin (magnitude). |

#### Typical Uses
- Get the direction and speed from a velocity vector.
- Convert position to angle for rotation effects.

---

### Polar to Cartesian (RadToCart)

> Converts polar coordinates (angle, length) to a 2D position (X, Y).

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Angle** | NumericalValue | `0` | Angle in degrees. |
| **Length** | NumericalValue | `0` | Distance/magnitude. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | 2D vector `(X, Y)`. |

#### Typical Uses
- Convert angle + speed into a velocity vector.
- Create circular motion by animating the angle.

---

## Position & Shape

### Shape

> Generates positions on or within a geometric shape.

Produces random points on/inside a square or ellipse based on alpha input.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Alpha** | NumericalValue | *particle alpha* | Maps to angle around shape (0-1). |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | 2D position `(X, Y)` on the shape. |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Shape | Square (`0`) | Shape type: `0` = Square, `1` = Ellipse. |
| Position | `(0, 0)` | Center position of the shape. |
| Size | `(1, 1)` | Width and height of the shape. |

#### Typical Uses
- Spawn particles in a circular pattern (ellipse shape).
- Emit from a rectangular area (square shape).
- Connect to **Offset** input of [Particle Module](#particle-module).

---

### Offset

> Interpolates particle position between two shapes over time.

Defines a "from" shape and a "to" shape. Each shape can be a square, ellipse, or line with configurable side constraints. Particles move from positions on the low shape to positions on the high shape based on alpha.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Alpha** | NumericalValue | *particle alpha* | Blend factor between low and high shapes (0-1). |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | Interpolated 2D position `(X, Y)`. |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Low Shape | Square (`0`) | Shape type for start: `0` = Square, `1` = Ellipse, `2` = Line. |
| High Shape | Square (`0`) | Shape type for end: `0` = Square, `1` = Ellipse, `2` = Line. |
| Low Edge | `true` | `true` = on edge, `false` = fill interior. |
| High Edge | `true` | `true` = on edge, `false` = fill interior. |
| Low Side | Bottom (`2`) | Constrain to side: `0` = All, `1` = Top, `2` = Bottom, `3` = Left, `4` = Right. |
| High Side | Right (`4`) | Constrain to side: `0` = All, `1` = Top, `2` = Bottom, `3` = Left, `4` = Right. |
| Low Position | `(0, 0)` | Center of the low shape. |
| Low Size | `(1, 1)` | Size of the low shape. |
| High Position | `(0, 0)` | Center of the high shape. |
| High Size | `(1, 1)` | Size of the high shape. |
| Tolerance | `0` | Randomization tolerance. |
| Curve Points | `[(0, 0.5)]` | Controls interpolation curve between low and high. |

#### Typical Uses
- Particles move from a point to a circle (converging/diverging patterns).
- Create waterfall effects: spawn on a line, fall to a bottom line.
- Complex spawn-to-destination animations.

---

### From To

> Calculates midpoint, angle, and distance between two points.

Given two positions, outputs the geometric relationship between them.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **From** | NumericalValue | `(0, 0)` | Start position. |
| **To** | NumericalValue | `(0, 0)` | End position. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Position** | NumericalValue | Midpoint between From and To `((from+to)/2)`. |
| **Rotation** | NumericalValue | Angle from From to To (degrees). |
| **Length** | NumericalValue | Distance between From and To. |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Default From | `(0, 0)` | Fallback start position. |
| Default To | `(0, 0)` | Fallback end position. |

#### Typical Uses
- Orient a particle between two points (e.g., laser beam, lightning).
- Calculate direction for projectile effects.
- Get the length for scaling a stretched sprite.

---

### Target

> Calculates trajectory toward a target position over time.

Computes the path from a start position to a destination, interpolating the position by alpha and outputting angle and velocity.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Velocity** | NumericalValue | `0` | Travel speed. |
| **From** | NumericalValue | `(0, 0)` | Start position. |
| **To** | NumericalValue | `(0, 0)` | Target/destination position. |
| **Alpha** | NumericalValue | *particle alpha* | Progress along the path (0-1). |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Time** | NumericalValue | Calculated travel time (`distance / velocity`). |
| **Position** | NumericalValue | Interpolated 2D position along the path. |
| **Velocity** | NumericalValue | Output velocity. |
| **Angle** | NumericalValue | Direction angle from From to To (degrees). |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Default Velocity | `0` | Fallback speed. |
| Default From | `(0, 0)` | Fallback start position. |
| Default To | `(0, 0)` | Fallback target position. |

#### Typical Uses
- Guide particles toward a point (homing missiles, convergence effects).
- Calculate travel time for synchronized animations.
- Create "seeking" particle behaviors.

---

## Forces & Physics

### Force Applier

> Converts a force vector into angle and velocity.

Takes the sum of force vectors and decomposes into direction (angle) and speed (velocity), accounting for elapsed time. Uses the formula `V = F * T` (mass = 1).

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Sum Forces** | NumericalValue | `(0, 0)` | Combined 2D force vector. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Angle** | NumericalValue | Direction of the force (degrees). |
| **Velocity** | NumericalValue | Speed magnitude (`|force| * deltaTime`). |

#### Typical Uses
- Convert combined forces (wind, gravity, attraction) into particle motion.
- Connect **Angle** and **Velocity** outputs to [Particle Module](#particle-module) inputs.

---

### Attractor

> Blends between initial trajectory and attraction toward a point.

Interpolates between a particle's initial velocity/direction and a vector pointing toward an attractor position, based on alpha.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Initial Angle** | NumericalValue | `0` | Starting movement direction (degrees). |
| **Initial Velocity** | NumericalValue | `0` | Starting speed. |
| **Attractor Position** | NumericalValue | `(0, 0)` | 2D position of the attractor point. |
| **Alpha** | NumericalValue | *particle alpha* | Blend factor: `0` = initial trajectory, `1` = fully attracted. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Angle** | NumericalValue | Blended direction (degrees). |
| **Velocity** | NumericalValue | Blended speed. |

#### Typical Uses
- Particles that start moving outward then curve toward a collection point.
- Vortex/spiral effects by animating the attractor position.
- Magnetic/gravity well effects.

---

### Vector Field

> Samples a vector field (FGA file) at the particle's position.

Loads an external vector field asset and samples it using the particle's position. The resulting vector is decomposed into angle and velocity. The field can be scaled in size and force.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Size Scale** | NumericalValue | `1.0` | Scales the spatial extent of the vector field. |
| **Force Scale** | NumericalValue | `1.0` | Multiplier for the output force magnitude. |
| **Position** | NumericalValue | `(0, 0)` | Offset position for field sampling. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Angle** | NumericalValue | Direction of the field vector at particle position (degrees). |
| **Velocity** | NumericalValue | Magnitude of the field vector (scaled). |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| FGA File | *(none)* | Path to the vector field asset file (`.fga` format). |

#### Typical Uses
- Complex wind/flow patterns from pre-authored vector fields.
- Turbulence and swirl effects.
- Any effect requiring spatially-varying forces.

---

## Drawables & Rendering

### Texture

> Provides a texture region as a particle drawable.

Loads a named texture from the asset provider and outputs it as a drawable.

#### Inputs

None.

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | DrawableValue | The loaded texture as a drawable. |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Region Name | *(none)* | Name of the texture region in your atlas/assets. |

#### Typical Uses
- The most common way to assign a sprite to particles.
- Connect to **Drawable** input of [Particle Module](#particle-module).

---

### Flipbook

> Animates through frames of a sprite sheet over time.

Divides a texture into a grid of frames and selects the current frame based on elapsed time.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Phase** | NumericalValue | *total time* | Time value driving the animation. Defaults to `TOTAL_TIME` from scope. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | DrawableValue | Current frame of the sprite sheet animation. |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Region Name | *(none)* | Name of the sprite sheet texture region. |
| Rows | `1` | Number of rows in the sprite sheet grid. |
| Cols | `1` | Number of columns in the sprite sheet grid. |
| Duration | `1.0` | Total animation cycle duration (seconds). |

#### Typical Uses
- Animated explosions, fire, smoke from sprite sheets.
- Any frame-by-frame particle animation.

---

### Nine Patch

> Wraps a drawable in a nine-patch for scalable rendering.

Takes an input drawable and makes it stretchable using nine-patch splits. The corners remain fixed while edges and center stretch.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Input** | DrawableValue | *(none)* | Source drawable to convert to nine-patch. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | DrawableValue | Nine-patch version of the input drawable. |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Splits | `[0, 0, 0, 0]` | Four values `[left, right, top, bottom]` defining the nine-patch boundaries in pixels. |

#### Typical Uses
- Scalable UI-like particles (speech bubbles, health bars).
- Particles that stretch without distorting their edges.

---

### Shader Material

> Separates surface shading from geometry, with dynamic uniform exposure through the node graph.

Loads a shader descriptor file (`.shdr`) and creates a `ParticleMaterial` that can be attached to **any** drawable — sprites, ribbons, polylines, flipbooks, or nine-patches. The node acts as a **material applicator**: connect any drawable to its input, and it comes out the other side with the shader material attached.

When the shader is loaded, the node automatically **parses GLSL uniforms** and generates dynamic input ports for each `uniform float`, `uniform vec2`, `uniform vec3`, or `uniform vec4` found in the shader code. Texture uniforms (`sampler2D`) are resolved from assets automatically.

This replaces the old [Shaded Sprite](#shaded-sprite-deprecated) node, which coupled shading and geometry into a single monolithic class.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Drawable** | DrawableValue | *(none)* | The geometry drawable to apply the shader to. Connect a [Texture](#texture), [Flipbook](#flipbook), [Nine Patch](#nine-patch), or any other drawable node. If not connected, a default 1x1 white quad is used (pure shader effect). |
| *(dynamic)* | NumericalValue | *(shader default)* | One input port is auto-generated for each numeric uniform in the shader. Port names match the uniform names (e.g., `u_distortionSpeed`, `u_noiseScale`). |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | DrawableValue | The input drawable with the `ParticleMaterial` attached. Connect directly to [Particle Module](#particle-module), or to [Ribbon](#ribbon) / [Polyline](#polyline) drawable inputs. |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Shader File | *(none)* | Path to the `.shdr` shader descriptor asset. Drop a `.shdr` file onto the node to load it. |

#### How It Works

1. **Load**: Drop a `.shdr` file onto the node. The shader is compiled and texture uniforms are resolved from assets.
2. **Connect Geometry**: Connect any drawable node (Texture, Flipbook, Nine Patch, etc.) to the **Drawable** input. The shader will be applied to that geometry. If no drawable is connected, a 1x1 white quad is used (for pure procedural shader effects).
3. **Dynamic Ports**: For each numeric uniform in the shader, an input port appears on the node. Connect any value generator (Curve, Math, Random Range, etc.) to drive that uniform per-particle.
4. **Material Propagation**: The output drawable carries a `ParticleMaterial`. All downstream drawables (Ribbon, Polyline, etc.) automatically use the material for rendering.
5. **Per-Particle Evaluation**: Because the node graph evaluates per-particle, each particle can have different uniform values based on its alpha, seed, or any other graph-driven data.

#### Architecture: Shape + Material Separation

The Shader Material node is part of the **Hybrid Shape + Material** architecture:

- **Shape** (geometry) is handled by drawables: `TextureRegionDrawable`, `RibbonRenderer`, `PolylineRenderer`, `SpriteAnimationDrawable`, `NinePatchDrawable`.
- **Material** (surface shading) is handled by `ParticleMaterial`: shader program, texture bindings, dynamic uniforms.
- Any drawable can carry an optional material. If no material is assigned, the drawable falls back to default sprite/color rendering.

#### Data Flow Examples

**Shader on a sprite** (the most common case):
```
[Texture] ──→ Drawable ──→ [Shader Material] ──→ Drawable ──→ [Particle Module]
[Curve]   ──→ u_dissolve ──┘
```

**Shader on a flipbook animation**:
```
[Flipbook] ──→ Drawable ──→ [Shader Material] ──→ Drawable ──→ [Particle Module]
[Math]     ──→ u_glow    ──┘
```

**Shader on a ribbon trail**:
```
[Texture]  ──→ Drawable ──→ [Shader Material] ──→ Ribbon Region ──→ [Ribbon] ──→ Drawable ──→ [Particle Module]
[Curve]    ──→ u_distort ──┘
```

**Pure procedural shader** (no drawable input):
```
[Shader Material] ──→ Drawable ──→ [Particle Module]
[Input]  ──→ u_time ──┘
```

At runtime, for each particle:
1. The upstream nodes compute values based on the particle's alpha/seed.
2. `ShaderModule.processValues()` stamps the material onto the incoming drawable and writes uniform values into the material's uniform map.
3. The drawable's `draw()` method calls `material.bind()` → the shader and uniforms are sent to the GPU.
4. Geometry is drawn with the custom shader active.
5. `material.unbind()` restores the previous shader.

#### Typical Uses
- **Dissolve/distortion on sprites**: Connect a Texture to the Drawable input, drive `u_dissolveAmount` with a Curve over particle lifetime.
- **Glowing flipbook animations**: Connect a Flipbook to the Drawable input, add a glow shader.
- **Shader-driven ribbons**: Connect through Ribbon's region inputs — material propagates automatically.
- **Procedural effects**: Leave Drawable unconnected for pure shader-quad effects (same as the old Shaded Sprite).
- **Nine-patch with shaders**: Connect a Nine Patch to the Drawable input for scalable UI particles with shader effects.

---

### Shaded Sprite (Deprecated)

> **Deprecated.** Use [Shader Material](#shader-material) instead.

Creates a drawable with a custom shader. This node couples shading and geometry into a single class, which limits flexibility. It is kept for backward compatibility with existing `.tls` effect files.

For new effects, use [Shader Material](#shader-material) which separates shading from geometry and supports dynamic uniform ports.

#### Inputs

None.

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | DrawableValue | Shader-rendered drawable (1x1 white quad with custom shader). |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Shader File | *(none)* | Name of the `.shdr` shader descriptor asset. |
| Texture Map | *(empty)* | Maps shader uniform names to texture regions. |

#### Migration Path
To migrate from Shaded Sprite to Shader Material:
1. Replace the Shaded Sprite node with a Shader Material node.
2. Load the same `.shdr` file.
3. If the shader was connected to a Ribbon's **Ribbon Region** input, the material will now propagate automatically — no `instanceof` dispatch needed.
4. Connect value generators to the new dynamic uniform ports for data-driven control.

---

### Building Shaders That Use the Particle's Texture (Shader Graph ↔ Particle Graph Bridge)

When building a `.shdr` file in the **Shader Graph Editor**, you need to sample the particle's sprite texture — the one connected to the Shader Material node's **Drawable** input in the particle graph. This is done with the **Input Texture** node.

#### The Two Texture Nodes in the Shader Graph

| Shader Graph Node | Samples | Use Case |
|-------------------|---------|----------|
| **Input Texture** | `u_texture` (texture unit 0) | The particle's drawable texture — whatever TextureRegion is connected to ShaderModule's Drawable input at runtime. |
| **Texture Sample** | `u_texture0`, `u_texture1`... (extra units) | A texture baked into the shader itself (e.g., a noise texture, distortion map). Declared as a `sampler2D` uniform. |

#### Input Texture Node

> Samples the particle drawable's texture (`u_texture`).

This is the entry point for accessing the sprite/flipbook/texture that the particle system provides. At runtime, LibGDX's SpriteBatch binds the drawable's texture to unit 0 as `u_texture`.

##### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **UV** | vec2 | `v_texCoords` | UV coordinates for sampling. Connect a UV manipulation node (Twirl, Rotate, Tiling) to distort the texture. |

##### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **RGBA** | vec4 | Full color sample from the texture. |
| **R** | float | Red channel. |
| **G** | float | Green channel. |
| **B** | float | Blue channel. |
| **A** | float | Alpha channel. |

#### Example: Dissolve Shader

**In the Shader Graph Editor** (builds the `.shdr` file):
```
[Input Texture] ──RGBA──→ inputColor ──→ [Color Mixer / Math] ──→ [Pixel Shader]
[Gradient Noise] ──→ step edge ──→ [Step Node] ──→ alpha mask ──┘
                     [Float Uniform "u_dissolve"] ──→ edge ──┘
```

The `Input Texture` node reads from `u_texture` — no file to drop, it just represents "the incoming sprite." The `Float Uniform "u_dissolve"` becomes a dynamic input port on the particle graph's Shader Material node.

**In the Particle Graph** (uses the compiled `.shdr` file):
```
[Texture "fire.png"] ──→ Drawable ──→ [Shader Material "dissolve.shdr"] ──→ Drawable ──→ [Particle Module]
                                       [Curve] ──→ u_dissolve ──────────┘
```

At runtime: the Texture node provides `fire.png` as the drawable. The Shader Material stamps its shader onto it. When the particle draws, `u_texture` = `fire.png`, and `u_dissolve` = the curve value for that particle's lifetime.

---

### Polyline

> Renders a multi-point curve with configurable thickness, color, and tangents.

Samples input values at multiple interpolated points to draw a smooth curve/line.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Offset** | NumericalValue | `(0, 0)` | Position offset for each point on the line. |
| **Thickness** | NumericalValue | `0.1` | Width of the line at each point. |
| **Color** | NumericalValue | `(1, 1, 1)` | RGB color at each point. |
| **Transparency** | NumericalValue | `1.0` | Alpha transparency at each point. |
| **Left Tangent** | NumericalValue | `(0, 0)` | Left tangent for curve shaping. |
| **Right Tangent** | NumericalValue | `(0, 0)` | Right tangent for curve shaping. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | DrawableValue | The rendered polyline as a drawable. |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Point Count | `2` | Number of interpolation points along the line. |
| Region Name | *(none)* | Texture applied along the polyline. |

#### Typical Uses
- Lightning bolts, laser beams, magic trails.
- Any effect requiring a textured curve.

---

### Ribbon

> Renders a trailing ribbon that follows particle movement.

Maintains a history of particle positions and renders a continuous ribbon/trail. Supports thickness, color, and transparency along its length.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Main Region** | DrawableValue | *(none)* | Optional main sprite for the particle head (not part of ribbon). |
| **Ribbon Region** | DrawableValue | *(none)* | Texture applied to the ribbon trail. Can be a regular texture or a [Shader Material](#shader-material) drawable (material is auto-propagated to the ribbon renderer). |
| **Thickness** | NumericalValue | `0.1` | Width of the ribbon. |
| **Transparency** | NumericalValue | `1.0` | Alpha of the ribbon. |
| **Color** | NumericalValue | `(1, 1, 1)` | RGB color of the ribbon. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | DrawableValue | The rendered ribbon trail as a drawable. |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Detail | `10` | Number of ribbon segments (more = smoother). |
| Memory Duration | `10` | How long to keep trail history (seconds). |

#### Typical Uses
- Sword slash trails, comet tails, motion trails.
- Any effect that needs a persistent trail following particle movement.

---

## Input & Scope

### Input

> Reads a value from the runtime scope into the module graph.

Bridges runtime context values (like particle alpha, seed, position) into the node graph for use by other modules.

#### Inputs

None.

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | The scope value at the configured key. |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Scope Key | `0` | Which scope value to read. See [Scope Variables](#scope-variables) for available keys. |

#### Typical Uses
- Access particle lifetime alpha, seed, or position explicitly.
- Feed scope values into complex calculations.

---

### Global Scope

> Reads a dynamic value from the global scope.

Similar to [Input](#input) but accesses the dynamic value slots, which can be set programmatically at runtime from game code.

#### Inputs

None.

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | NumericalValue | Dynamic value from the global scope at the given key. |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Key | `0` | Index of the dynamic scope value (0-9). |

#### Typical Uses
- Pass runtime game values (player position, health, speed) into the particle effect.
- Create interactive effects that respond to gameplay.

---

### Emitter Config

> Provides emitter configuration flags.

Outputs a set of boolean options that control the emitter's behavior mode.

#### Inputs

None.

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Output** | EmConfigValue | Configuration flags bundle. |

#### Properties

| Flag | Default | Description |
|------|---------|-------------|
| Continuous | `true` | Emitter loops indefinitely (restarts after duration). |
| Attached | `false` | Particles follow the emitter's transform (move with parent). |
| Additive | `true` | Use additive blending (bright, glowy look). |
| Blend Add | `false` | Alternative additive blend mode. |
| Aligned | `false` | Particles rotate to align with their movement direction. |
| Immortal | `false` | Emitter never stops (ignores duration). |

#### Typical Uses
- Connect to the **Config** input of [Emitter Module](#emitter-module).
- Toggle between additive (fire, magic) and normal (smoke, debris) blending.
- Make particles stick to a moving emitter with **Attached**.

---

## Utility

### Fake Motion Blur

> Maps velocity to size for simulated motion blur.

Takes a velocity input, clamps it to a range, normalizes it, then maps it to a size output. Fast particles appear stretched/blurred.

#### Inputs

| Slot | Type | Default | Description |
|------|------|---------|-------------|
| **Velocity** | NumericalValue | `0` | Current particle velocity. |

#### Outputs

| Slot | Type | Description |
|------|------|-------------|
| **Size** | NumericalValue | Mapped size value based on velocity. |

#### Properties

| Property | Default | Description |
|----------|---------|-------------|
| Velocity Min | `0` | Minimum velocity threshold (below this = sizeMin). |
| Velocity Max | `0` | Maximum velocity threshold (above this = sizeMax). |
| Size Min | `0` | Output size at minimum velocity. |
| Size Max | `0` | Output size at maximum velocity. |

#### Typical Uses
- Stretch fast-moving particles to simulate motion blur.
- Connect output to the **Size** input of [Particle Module](#particle-module).

---

### NAN (Placeholder)

> Empty placeholder node with no functionality.

Has no inputs, no outputs, and does nothing. Exists as a placeholder in the system.

---

## Common Patterns & Recipes

### Basic Particle Setup
```
[Texture] --> Drawable --> [Particle Module]
[Random Range] --> Life --> [Particle Module]
[Random Range] --> Size --> [Particle Module]
[Random Range] --> Velocity --> [Particle Module]
[Random Range] --> Angle --> [Particle Module]
[Emitter Config] --> Config --> [Emitter Module]
```

### Animated Color Over Lifetime
```
[Gradient Color] --> Color --> [Particle Module]
```
*(Alpha defaults to particle lifetime - no connection needed)*

### Size Curve with Random Variation
```
[Dynamic Range] --> Size --> [Particle Module]
```
*(Set low/high min/max for variation, curve for shape over lifetime)*

### Circular Emission Pattern
```
[Random Range (0-360)] --> Angle --> [Particle Module]
[Shape (Ellipse)] --> Offset --> [Particle Module]
```

### Physics-Based Movement
```
[Vector Field] --Angle--> Angle --> [Particle Module]
[Vector Field] --Velocity--> Velocity --> [Particle Module]
```

### Trail Effect
```
[Texture] --> Ribbon Region --> [Ribbon] --> Drawable --> [Particle Module]
```

### Shader on a Sprite
```
[Texture] --> Drawable --> [Shader Material] --> Drawable --> [Particle Module]
                           [Curve] --> u_dissolve --|
```
*(Connect any drawable to Shader Material's input — the shader is applied to that geometry)*

### Shader on a Flipbook Animation
```
[Flipbook] --> Drawable --> [Shader Material] --> Drawable --> [Particle Module]
                            [Math] --> u_glow --|
```

### Shader-Driven Ribbon Trail
```
[Texture] --> Drawable --> [Shader Material] --> Ribbon Region --> [Ribbon] --> Drawable --> [Particle Module]
                           [Curve] --> u_distort --|
```

### Pure Procedural Shader (No Drawable Input)
```
[Shader Material] --> Drawable --> [Particle Module]
[Input (PARTICLE_ALPHA)] --> u_progress --|
```
*(With no Drawable connected, a 1x1 white quad is used — same as old Shaded Sprite)*

### Eased Fade-Out
```
[Input (PARTICLE_ALPHA)] --> [Interpolation (fadeOut)] --> Transparency --> [Particle Module]
```

### Oscillating Value
```
[Input (TOTAL_TIME)] --> A --> [Math (sin)] --> [Math (multiply)] --> Size --> [Particle Module]
[Static Value] ---------> B --|
```
