
#ifndef __EXTENSIONS__MODULES__SINK__LETHE__ADAPTER_H
#define __EXTENSIONS__MODULES__SINK__LETHE__ADAPTER_H

#include <utility>
	
namespace Extensions {
	namespace Modules {
        namespace Sink {
            namespace Lethe {
                template <class InputStream1, class InputStream2>
                class Adapter {
                public:
                    
                    typedef Core::Templates::ReadVector<typename InputStream1::buffer_type, Extensions::Types::Integer> SourceVector;
    
                    SourceVector source(size_t count)
                    {
                            assert (_inputStream1.first != 0);
                            assert (count <= 1);
                            typename InputStream1::buffer_type* data = 0;
                            size_t offset = 0;
                            size_t size = 0;

                            Core::StreamCallback callback = _inputStream1.first->read(count, count, _inputStream1.second, data, offset, size);

                            return SourceVector(data, count, offset, size, callback);
                    }

                    SourceVector source(size_t count, size_t forward)
                    {
                            assert (_inputStream1.first != 0);
                            assert (count <= 1);

                            typename InputStream1::buffer_type* data = 0;
                            size_t offset = 0;
                            size_t size = 0;

                            Core::StreamCallback callback = _inputStream1.first->read(count, forward, _inputStream1.second, data, offset, size);

                            return SourceVector(data, count, offset, size, callback);
                    }
                    
                    typedef Core::Templates::ReadVector<typename InputStream2::buffer_type, Extensions::Types::Integer> ResultVector;
    
                    ResultVector result(size_t count)
                    {
                            assert (_inputStream2.first != 0);
                            assert (count <= 1);
                            typename InputStream2::buffer_type* data = 0;
                            size_t offset = 0;
                            size_t size = 0;

                            Core::StreamCallback callback = _inputStream2.first->read(count, count, _inputStream2.second, data, offset, size);

                            return ResultVector(data, count, offset, size, callback);
                    }

                    ResultVector result(size_t count, size_t forward)
                    {
                            assert (_inputStream2.first != 0);
                            assert (count <= 1);

                            typename InputStream2::buffer_type* data = 0;
                            size_t offset = 0;
                            size_t size = 0;

                            Core::StreamCallback callback = _inputStream2.first->read(count, forward, _inputStream2.second, data, offset, size);

                            return ResultVector(data, count, offset, size, callback);
                    }
                    

                    void addInputStream(Core::Stream* stream, unsigned int streamId, unsigned int moduleId)
                    {
               		 switch(streamId) {
            			
                    	case 0: 
                            {
                    		InputStream1* inputStream1 = dynamic_cast <InputStream1*>(stream); 
                   			assert(inputStream1);
                   			_inputStream1 = std::make_pair(inputStream1, moduleId);
                            }
                   			break;
                    	
                    	case 1: 
                            {
                    		InputStream2* inputStream2 = dynamic_cast <InputStream2*>(stream); 
                   			assert(inputStream2);
                   			_inputStream2 = std::make_pair(inputStream2, moduleId);
                            }
                   			break;
                    			
                    	default:
                    		assert(false);
                    	}    
                    }
                    
                    void addOutputStream(Core::Stream* stream, unsigned int streamId, unsigned int moduleId)
                    {
                    	switch(streamId) {
            					
                    	default:
                    		assert(false);
                    	}
                    }

                private:
                    std::pair<InputStream1*, unsigned int> _inputStream1;
                    std::pair<InputStream2*, unsigned int> _inputStream2;
                    
                };

                enum Caller {
                	LETHE_SCHEDULER, SOURCE, RESULT
                };

                Caller inputPortMap[] = {SOURCE, RESULT};
                Caller outputPortMap[] = {};
            }
        }
    }
}

#endif
