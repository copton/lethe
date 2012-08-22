
#ifndef __EXTENSIONS__MODULES__SOURCE__LETHE__ADAPTER_H
#define __EXTENSIONS__MODULES__SOURCE__LETHE__ADAPTER_H

#include <utility>
	
namespace Extensions {
	namespace Modules {
        namespace Source {
            namespace Lethe {
                template <class OutputStream1>
                class Adapter {
                public:
                    
                    typedef Core::Templates::WriteVector<Extensions::Types::Integer, typename OutputStream1::buffer_type> OutputVector;
     
                    OutputVector output(size_t count, bool throwWhenStreamHasNoReaders=false)
                    {
                            assert (_outputStream1.first != 0);
                            assert (count <= 1);

                            typename OutputStream1::buffer_type* data = 0;
                            size_t offset = 0;
                            size_t size = 0;

                            Core::StreamCallback callback = _outputStream1.first->write(count, _outputStream1.second, throwWhenStreamHasNoReaders, data, offset, size);

                            return OutputVector(data, count, offset, size, callback);
                    }
                    

                    void addInputStream(Core::Stream* stream, unsigned int streamId, unsigned int moduleId)
                    {
               		 switch(streamId) {
            					
                    	default:
                    		assert(false);
                    	}    
                    }
                    
                    void addOutputStream(Core::Stream* stream, unsigned int streamId, unsigned int moduleId)
                    {
                    	switch(streamId) {
            			
                    	case 0: 
                            {
                    		OutputStream1* outputStream1 = dynamic_cast <OutputStream1*>(stream); 
                   			assert(outputStream1);
                   			_outputStream1 = std::make_pair(outputStream1, moduleId);
                            }
                   			break;
                    			
                    	default:
                    		assert(false);
                    	}
                    }

                private:
                    std::pair<OutputStream1*, unsigned int> _outputStream1;
                    
                };

                enum Caller {
                	LETHE_SCHEDULER, OUTPUT
                };

                Caller inputPortMap[] = {};
                Caller outputPortMap[] = {OUTPUT};
            }
        }
    }
}

#endif
