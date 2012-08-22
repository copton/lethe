
#include <core/graphLogic.h>
#include <core/application.h>
#include <core/tstream.h>
#include <types/Integer/Integer.h>
#include <types/Complex/Complex.h>
#include <modules/Source/Source.h>
#include <modules/Encoder/Encoder.h>
#include <modules/Channel/Channel.h>
#include <modules/Decoder/Decoder.h>
#include <modules/Sink/Sink.h>

class GraphLogic : public Core::GraphLogic {
public:
GraphLogic(const std::string jobId) : 
stream0(0),
stream1(0),
stream2(0),
stream3(0),
module0(0),
module1(0),
module2(0),
module3(0),
module4(0),
module5(0),
_jobId(jobId) 
{ }

void shutdown()
{
if (stream0) delete stream0;
if (stream1) delete stream1;
if (stream2) delete stream2;
if (stream3) delete stream3;

if (module0) delete module0;
if (module1) delete module1;
if (module2) delete module2;
if (module3) delete module3;
if (module4) delete module4;
if (module5) delete module5;
}
const std::string& getJobId() { return _jobId; }

void createGraph(std::vector<Core::Module*>& vertices, std::vector<Core::Stream*>& edges, Core::Controller& controller, const std::vector<Ice::InputStreamPtr>& isVector)
{
stream0 = new Stream0(0, 4, 2, 2, 2);
stream1 = new Stream1(1, 2, 1, 1, 1);
stream2 = new Stream2(2, 2, 1, 1, 1);
stream3 = new Stream3(3, 4, 2, 1, 1);

module0 = new Module0(0, "Quelle", 0, 1, controller, isVector[0]);
module1 = new Module1(1, "Quelle2", 0, 1, controller, isVector[1]);
module2 = new Module2(2, "Kodierer", 1, 1, controller, isVector[2]);
module3 = new Module3(3, "Kanal", 1, 1, controller, isVector[3]);
module4 = new Module4(4, "Dekodierer", 1, 1, controller, isVector[4]);
module5 = new Module5(5, "Senke", 2, 0, controller, isVector[5]);

module0->addOutputStream(stream0, 0, 0);
stream0->addWriter(module0, 0, 0);
module1->addOutputStream(stream0, 0, 1);
stream0->addWriter(module1, 0, 1);
module2->addInputStream(stream0, 0, 0);
stream0->addReader(module2, 0, 0);
module2->addOutputStream(stream1, 0, 0);
stream1->addWriter(module2, 0, 0);
module3->addInputStream(stream1, 0, 0);
stream1->addReader(module3, 0, 0);
module3->addOutputStream(stream2, 0, 0);
stream2->addWriter(module3, 0, 0);
module4->addInputStream(stream2, 0, 0);
stream2->addReader(module4, 0, 0);
module4->addOutputStream(stream3, 0, 0);
stream3->addWriter(module4, 0, 0);
module5->addInputStream(stream0, 0, 1);
stream0->addReader(module5, 0, 1);
module5->addInputStream(stream3, 1, 0);
stream3->addReader(module5, 1, 0);

edges.push_back(stream0);
edges.push_back(stream1);
edges.push_back(stream2);
edges.push_back(stream3);

vertices.push_back(module0);
vertices.push_back(module1);
vertices.push_back(module2);
vertices.push_back(module3);
vertices.push_back(module4);
vertices.push_back(module5);
}

private:
typedef Core::Templates::Stream<Foos, Extensions::Types::Integer> Stream0;
typedef Core::Templates::Stream<Extensions::Types::Complex, Extensions::Types::Complex> Stream1;
typedef Core::Templates::Stream<Extensions::Types::Complex, Extensions::Types::Complex> Stream2;
typedef Core::Templates::Stream<Extensions::Types::Integer, Extensions::Types::Integer> Stream3;

typedef Extensions::Modules::Source::Module<Stream0> Module0;
typedef Extensions::Modules::Source::Module<Stream0> Module1;
typedef Extensions::Modules::Encoder::Module<Stream0,Stream1> Module2;
typedef Extensions::Modules::Channel::Module<Stream1,Stream2> Module3;
typedef Extensions::Modules::Decoder::Module<Stream2,Stream3> Module4;
typedef Extensions::Modules::Sink::Module<Stream0,Stream3> Module5;

Stream0* stream0;
Stream1* stream1;
Stream2* stream2;
Stream3* stream3;
Module0* module0;
Module1* module1;
Module2* module2;
Module3* module3;
Module4* module4;
Module5* module5;

const std::string _jobId;
};
    
int main(int argc, char* argv[])
{
GraphLogic graphLogic("JOBID");
Core::Application application(graphLogic);
return application.main(argc, argv);
}
