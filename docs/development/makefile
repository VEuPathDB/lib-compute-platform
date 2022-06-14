GRAPHS = $(wildcard graphs/*.dot)
PNGS   = $(GRAPHS:.dot=.png)

.PHONY: nothing
nothing:
	@echo "What are you doing"

.PHONY: graphs
graphs: $(PNGS)

graphs/%.png: graphs/%.dot
	@dot $< -Tpng -o$@