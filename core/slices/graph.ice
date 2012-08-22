#ifndef __GRAPH_ICE__
#define __GRAPH_ICE__

module Comm {
	module Job {
		module Graph {
			struct PortDescriptor {
				string portName;
				string vertexName;
			};

			struct Port {
                PortDescriptor descriptor;

				string basicType;
                string usedType;
				int blocksize;
			};

			sequence<string> StringSeq;
			sequence<PortDescriptor> PortDescSeq;

			struct Edge {
                PortDescSeq writerPorts;
                PortDescSeq readerPorts;
                long minSize;
			};

            sequence<byte> Configuration;
			struct Vertex {
				string vertexName;

				string moduleName;
				Configuration config;

				StringSeq inputPorts;
				StringSeq outputPorts;
			};

			sequence<Edge> Edges;
			sequence<Vertex> Vertices;
			sequence<Port> Ports;

            struct Phase {
                PortDescSeq activePorts;
                PortDescSeq resetPorts;
                StringSeq observedVertices;
                StringSeq resultVertices;
            };

			sequence<Phase> Phases;

			struct SimulationGraph {
                Vertices theVertices;
                Edges theEdges;
                Ports thePorts;
                Phases thePhases;
			};
		};
	};
};

#endif
