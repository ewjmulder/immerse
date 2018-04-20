# Immerse - Audio Streaming

TODO: documentation about this module

- Mixer
// TODO: mention API to the outside is just lifecycle: initialize, start, stop and playScenario
// No 'leaking' of internal data like ActiveScenario, just communication with playback id's
// and mention the scenariosToActive synchronysation
- Warmup
- Steps
- ActiveScenario - with state
- UUID communication
- Default audio device - fallback upon exception
- TODO: write more information about the bug: asymmetric unsigned byte --> signed float --> signed byte conversion
- etc
