# Cape Town

This folder contains the data required to run the freight receivers for the
Pick n Pay scenario in the City of Cape Town.

### `network.xml.gz`

The road network was parsed from _OpenStreetMap_ and contains the functional
area of the City of Cape Town. Contrary to normal (South African) networks,
which includes the coarse national network for freight vehicles, we only use
the city's network. That said, we clip the coarse network to the boundary to
reduce computational burden. On the `ie-susie` server, this means we're using the 
`capeTownOnly_coarse_clean.xml.gz` file. The current network used was created on
28 September 2018 at 15:00.

## `facilities.xml.gz`

The facilities were also parsed from _OpenStreetMap_ on `ie-susie`. We use the
latest (28 September 2018 at 00:34) file from the server called `capeTown_latest.pbf`
and then parsing the Pick n Pays using the java class `receiver.wlbCapeTown.ParsePnP`.
