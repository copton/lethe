def idGenerator():
    id = 0
    while True:
        yield id
        id += 1

def descToTuple(descriptor):
    return (descriptor.vertexName, descriptor.portName)

def addUnique(item, list):
    if not item in list:
        list.append(item)

class Vertex:
    def __init__(self, number, vertex):
        self.uid = vertex.vertexName
        self.number = number
        self.inputPorts = []
        self.outputPorts = []
        self.moduleName = vertex.moduleName

    def __str__(self):
        lines = []
        for item in ["uid", "number", "moduleName"]:
            lines += ["%s: %s" % (item, str(getattr(self, item)))]
        lines += ["inputPorts"]
        lines += [str(port.uid) for port in self.inputPorts]
        lines += ["outputPorts"]
        lines += [str(port.uid) for port in self.outputPorts]
        return '\n'.join(lines)

class Port:
    INPUT = 0
    OUTPUT = 1
    def __init__(self, number, port):
        self.uid = descToTuple(port.descriptor)
        self.number = number
        self.vertex = None
        self.edge = None
        self.basicType = port.basicType
        self.usedType = port.usedType
        self.blocksize = port.blocksize
        self.dir = None
    
    def __str__(self):
        lines = []
        for item in ["uid", "number", "basicType", "usedType", "blocksize"]:
            lines += ["%s: %s" % (item, str(getattr(self, item)))]
        lines += ["dir: %s" % ({ Port.INPUT : "Input", Port.OUTPUT : "Output" }[self.dir])] 
        lines += ["vertex: %s" % self.vertex.uid]
        lines += ["edge: %s" % self.edge.number]
        return '\n'.join(lines)

class Edge:
    def __init__(self, number, edge):
        self.number = number
        self.writerPorts =  []
        self.readerPorts = []
        self.minSize = edge.minSize

    def __str__(self):
        lines = []
        lines += ["number: %d" % self.number]
        lines += ["writerPorts"]
        lines += [str(port.uid) for port in self.writerPorts]
        lines += ["readerPorts"]
        lines += [str(port.uid) for port in self.readerPorts]
        return '\n'.join(lines)

def prepareJob(job):
    print "Generator: codegen: prepareJob"

    ports = []

    def findPort(uid):
        for port in ports:
            if port.uid == uid:
                return port
        assert False

    types = []
    g = idGenerator()
    for port in job.theGraph.thePorts:
        addUnique(port.basicType, types)
        ports.append(Port(g.next(), port))

    modules = []
    vertices = []
    g = idGenerator()
    for vertex in job.theGraph.theVertices:
        addUnique(vertex.moduleName, modules)
        newVertex = Vertex(g.next(), vertex)
        for portName in vertex.inputPorts:
            port = findPort((vertex.vertexName, portName))
            newVertex.inputPorts.append(port)
            port.vertex = newVertex
            port.dir = Port.INPUT
        for portName in vertex.outputPorts:
            port = findPort((vertex.vertexName, portName))
            newVertex.outputPorts.append(port)
            port.vertex = newVertex
            port.dir = Port.OUTPUT
        vertices.append(newVertex)

    edges = []
    g = idGenerator()
    for edge in job.theGraph.theEdges:
        newEdge = Edge(g.next(), edge)
        for portDesc in edge.writerPorts:
            port = findPort(descToTuple(portDesc))
            newEdge.writerPorts.append(port)
            port.edge = newEdge
        for portDesc in edge.readerPorts:
            port = findPort(descToTuple(portDesc))
            newEdge.readerPorts.append(port)
            port.edge = newEdge
        edges.append(newEdge)
        
    print "*** PORTS"
    for port in ports:
        print port
        print "------"
    print "*** VERTICES"
    for vertex in vertices:
        print vertex
        print "------"
    print "*** EDGES"
    for edge in edges:
        print edge
        print "------"

    return (vertices, edges, ports, modules, types, job.jobId)


def generateConfigMak((vertices, edges, ports, modules, types, jobId), file):
    print "Generator: codegen: generateConfigMak"
    file.write("modules_LIST = " + " ".join(modules) + '\n')
    file.write("types_LIST = " + " ".join(types) + '\n')

class GenerateMain:
    def __init__(self, (vertices, edges, ports, modules, types, jobId)):
        self.vertices = vertices
        self.edges = edges
        self.ports = ports
        self.modules = modules
        self.types = types
        self.jobId = jobId

    def __getitem__(self, name):
        function = getattr(self, name)
        return function()

    def types_include(self):
        template = "#include <types/%(name)s/%(name)s.h>"
        lines = [template % { 'name' : type } for type in self.types]
        return '\n'.join(lines)

    def modules_include(self):
        template = "#include <modules/%(name)s/%(name)s.h>"
        lines = [template % { 'name' : module } for module in self.modules]
        return '\n'.join(lines)

    def jobid(self):
        return self.jobId

    def streams_init(self):
        def getParams(edge):
            return {
                'number' : edge.number
            }
        template = "stream%(number)d(0),"
        lines = [template % getParams(edge) for edge in self.edges]
        return '\n'.join(lines)

    def modules_init(self):
        def getParams(vertex):
            return {
                'number' : vertex.number
            }
        template = "module%(number)d(0),"
        lines = [template % getParams(vertex) for vertex in self.vertices]
        return '\n'.join(lines)

    def streams_create(self):
        def getParams(edge):
            writerBs = [port.blocksize for port in edge.writerPorts]
            readerBs = [port.blocksize for port in edge.readerPorts]
            writerBs.sort()
            readerBs.sort()

            size = readerBs[-1] - 1 + writerBs[-1]
            if size < edge.minSize:
                size = edge.minSize
            
            return {
                'number' : edge.number,
                'size' : size,
                'numberofReaders' : len(edge.readerPorts),
                'numberofWriters' : len(edge.writerPorts),
                }

        template = "stream%(number)d = new Stream%(number)d(%(number)d, %(size)d, %(numberofReaders)d, %(numberofWriters)d);"
        lines = [template % getParams(edge) for edge in self.edges]
        return '\n'.join(lines)

    def modules_create(self):
        def getParams(vertex):
            return {
                'number' : vertex.number,
                'vertexName' : vertex.uid,
                'numberofInputStreams' : len(vertex.inputPorts),
                'numberofOutputStreams' : len(vertex.outputPorts)
                }
        template = 'module%(number)d = new Module%(number)d(%(number)d, "%(vertexName)s", %(numberofInputStreams)d, %(numberofOutputStreams)d, controller, isVector[%(number)d]);'
        lines = [template % getParams(vertex) for vertex in self.vertices]
        return '\n'.join(lines)

    def streams_delete(self):
        def getParams(edge):
            return {
                'number' : edge.number
            }
        template = "if (stream%(number)d) delete stream%(number)d;"
        lines = [template % getParams(edge) for edge in self.edges]
        return '\n'.join(lines)

    def modules_delete(self):
        def getParams(vertex):
            return {
                'number' : vertex.number
            }
        template = "if (module%(number)d) delete module%(number)d;"
        lines = [template % getParams(vertex) for vertex in self.vertices]
        return '\n'.join(lines)
        

    def graph(self):
        def getParams(port):
            if port.dir == Port.INPUT:
                dir = "Input"
                type = "Reader"
                edgeId = port.vertex.inputPorts.index(port)
                vertexId = port.edge.readerPorts.index(port)
            elif port.dir == Port.OUTPUT:
                dir = "Output"
                type = "Writer"
                vertexId = port.edge.writerPorts.index(port)
                edgeId = port.vertex.outputPorts.index(port)
            else:
                assert False

            return {
                'vertexNumber' : port.vertex.number,
                'edgeNumber' : port.edge.number,
                'dir' : dir,
                'type' : type,
                'edgeId' : edgeId,
                'vertexId' : vertexId,
                }
                
        template = "module%(vertexNumber)d->add%(dir)sStream(stream%(edgeNumber)d, %(edgeId)d, %(vertexId)d);"
        template += "\nstream%(edgeNumber)d->add%(type)s(module%(vertexNumber)d, %(edgeId)d, %(vertexId)d);"

        lines = [template % getParams(port) for port in self.ports]
        return '\n'.join(lines)

    def modules_register(self):
        def getParams(vertex):
            return {
                'number' : vertex.number
                }
        template = "vertices.push_back(module%(number)d);"
        lines = [template % getParams(vertex) for vertex in self.vertices]
        return '\n'.join(lines)

    def streams_register(self):
        def getParams(edge):
            return {
                'number' : edge.number
                }

        template = "edges.push_back(stream%(number)d);"
        lines = [template % getParams(edge) for edge in self.edges]
        return '\n'.join(lines)

    def streams_typedef(self):
        def getParams(edge):
            def allEqual(list):
                value = list[0]
                for item in list:
                    if value != item:
                        return False
                return True

            ice_type = edge.writerPorts[0].basicType

            used_types = [port.usedType for port in edge.writerPorts + edge.readerPorts if port.usedType != '']

            if len(used_types) != 0 and allEqual(used_types):
                buffer_type = used_types[0]
            else:
                buffer_type = "Extensions::Types::" + ice_type
            
            return {
                'number' : edge.number,
                'ice_type' : ice_type,
                'buffer_type' : buffer_type,
                }

        template = "typedef Core::Templates::Stream<%(buffer_type)s, Extensions::Types::%(ice_type)s> Stream%(number)d;"
        lines = [template % getParams(edge) for edge in self.edges] 
        return '\n'.join(lines)

    def modules_typedef(self):
        def getParams(vertex):
            items = ["Stream%d" % port.edge.number for port in vertex.inputPorts]
            items += ["Stream%d" % port.edge.number for port in vertex.outputPorts]
            types = ','.join(items)
                
            return {
                'number' : vertex.number,
                'type' : vertex.moduleName,
                'stream_types' : types,
                }
            
        template = "typedef Extensions::Modules::%(type)s::Module<%(stream_types)s> Module%(number)d;"
        lines = [template % getParams(vertex) for vertex in self.vertices]
        return '\n'.join(lines)
        
    def members(self):
        template = "Stream%(number)d* stream%(number)d;"
        lines = [template % { 'number' : edge.number } for edge in self.edges]
        template = "Module%(number)d* module%(number)d;"
        lines += [template % { 'number' : vertex.number } for vertex in self.vertices]
        return '\n'.join(lines)

def generateMain(preparedJob, file):
    print "Generator: codegen: generateMain"
    
    template = """
#include <core/graphLogic.h>
#include <core/application.h>
#include <core/tstream.h>
%(types_include)s
%(modules_include)s

class GraphLogic : public Core::GraphLogic {
public:
GraphLogic(const std::string jobId) : 
%(streams_init)s
%(modules_init)s
_jobId(jobId) 
{ }

void shutdown()
{
%(streams_delete)s

%(modules_delete)s
}
const std::string& getJobId() { return _jobId; }

void createGraph(std::vector<Core::Module*>& vertices, std::vector<Core::Stream*>& edges, Core::Controller& controller, const std::vector<Ice::InputStreamPtr>& isVector)
{
%(streams_create)s

%(modules_create)s

%(graph)s

%(streams_register)s

%(modules_register)s
}

private:
%(streams_typedef)s

%(modules_typedef)s

%(members)s

const std::string _jobId;
};
    
int main(int argc, char* argv[])
{
GraphLogic graphLogic("%(jobid)s");
Core::Application application(graphLogic);
return application.main(argc, argv);
}
"""
    file.write(template % GenerateMain(preparedJob))
