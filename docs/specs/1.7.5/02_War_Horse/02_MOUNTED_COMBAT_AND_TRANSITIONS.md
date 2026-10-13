# War horse — mounted combat and transitions

## Mounted phase concept
Kalin can start mounted.
Mounted phase focuses on mobility and battlefield dominance.

## Mounted abilities
1. Cavalry Charge
   - Long straight-line rush, knocks targets aside.
2. Mounted Circular Slash
   - Kalin swings sabre in a wide arc while moving.
3. Horse Burst Dash
   - Short speed burst to close distance.
4. Hoof Stomp
   - Local shockwave around horse if the player stays too close.
5. Battle Order from Saddle
   - Kalin issues a command while mounted.

## Dismount trigger
Recommended fast implementation:
- Mounted phase until ~65% boss HP.
- Then scripted dismount and start of stronger infantry-focused duel.

## Dismount animation
- Horse slows / rears
- Kalin swings off to the ground
- Horse remains for a short retreat animation and then exits combat
