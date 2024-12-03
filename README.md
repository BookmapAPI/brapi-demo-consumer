# BrAPI Demo Consumer

The repository consists of three add-ons:
- **Demo Consumer** shows how to use BrAPI as a consumer fully, it can connect to many providers. 
- **Simple Demo Consumer** shows how to use BrAPI in a basic consumer, it connects only to the Absorption Indicator.
- **Simple Demo Provider** shows how to use BrAPI as a basic provider, it calculates the moving average, broadcasts live events, and shows them on the panel.

## Idea
The idea is to show how to work with the Broadcasting API to create an add-on that will listen to any add-on event provider.
<br>Demo Consumer (DC) is an add-on that connects to one of the available providers using the Broadcasting API(BrAPI) 
library and visualizes in its panel the events received via Broadcasting API.

Providers that DemoConsume supports:
- Absorption Indicator (AI)
- Sweep Indicator
- Stops and Icebergs On-Chart
- B3 Icebergs On-Chart
- Market Pulse

## Dependencies
- Broadcasting API, the latest version is located locally in the mavenLib directory, you can also find the javadoc there.
- BrModule provider. Jar with provider classes. Individual for each provider. BrModule of the Absorption Indicator is in the folder "providers".

## How to work
1. Get an instance of BroadcasterConsumer. This is the main interface for working with the BrAPI. To get it, use the factory:
```
    BroadcasterConsumer broadcasterConsumer = BroadcastFactory.getBroadcasterConsumer(provider,ADDON_NAME, this.getClass());
```

2. Set the Broadcasting lifecycle. At what point in time your add-on will work with Broadcasting.
```
    broadcaster.start();
    broadcaster.finish();
```

3. Connect to provider add-on. Add-ons must connect before requesting any data or subscribing to a live broadcast.
```
    broadcasterConsumer.connectToProvider(providerName,connectionListener);
```
Where connectionListener is your implementation of ConnectionStatusListener. Broadcasting will notify you when the connection status changes through this interface.

4. Get the data.
- You can subscribe to live events of provider generators. In AI, generators per instrument and have a name equal to the alias.
```
    broadcasterConsumer.subscribeToLiveData(PROVIDER_NAME, generatorInfo.getGeneratorName(),
        EventInterface.class, eventListener, subscriptionListener);
```
- You can get the DataStructureInterface of the provider. And then make queries to it.
```
    dataStructureInterface = broadcasterConsumer.getDataStructureInterface(PROVIDER_NAME);
    List<Object> objects = dataStructureInterface.get(DemoConsumer.class, alias, timeIntervalStartTime, provider.getCurrentTime(), alias);
```

5. After you get something from the provider (events, filter, settings), you must make a cast for these objects with CastUtilities.
```
    EventInterface event = CastUtilities.castObject(o, TradeEvent.class);
```

6. To apply a filter. Generators have a filter and settings. 
You get all events that the generator generates based on its settings. 
The consumer cannot change the generator settings. 
To get the same data that the provider visualizes, you must apply the provider's filter to the events.
You can get a list of filter information from which you can get the filter and settings.
```
    List<GeneratorInfo> generatorsInfo = broadcasterConsumer.getGeneratorsInfo(PROVIDER_NAME);
```
You can also set the UpdateFilterListener and UpdateSettingsListener in GeneratorInfo to get the filter and settings when they are updated.

